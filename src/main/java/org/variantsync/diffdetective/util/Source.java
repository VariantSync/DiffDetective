package org.variantsync.diffdetective.util;

import java.nio.file.Path; // For Javadoc
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.diff.git.PatchDiff; // For Javadoc
import org.variantsync.diffdetective.variation.diff.ProjectionSource;
import org.variantsync.diffdetective.variation.diff.VariationDiff; // For Javadoc
import org.variantsync.functjonal.Cast;

/**
 * An interface for source traceability.
 * For testing and debugging, it can be really useful to know where some value originated from. For
 * example, imagine an error during you analysis of a {@link VariationDiff}. Now you want to know
 * where the variation diff came from. The solution: DiffDetectice tracks the source and
 * transformations of important data structures like {@link VariationDiff#getSource()} and allows
 * you to inspect them using this interface.
 * <p>
 * {@link Source} represents a hierarchy of inputs, processing steps, and results (the distinction
 * between these types is not directly reflected in the API and only serve as explanation of the
 * interface).
 * <ul>
 * <li>An input (e.g., {@link FileSource}) is a {@link Source} that has no {@link getSources
 * children}. Inputs are the leafs of the {@link Source} hierarchy and contain the {@link
 * getSourceExplanation name of the input} (e.g., {@code "file"} or {@code "URL"}) and {@link
 * getSourceArguments some arguments} (e.g., the file name or URL).
 * <li>A step (e.g., {@link ProjectionSource}) processed {@link getSources one or more sources}, has
 * a {@link getSourceExplanation name} and may be configurable by some {@link getSourceArguments
 * arguments}. Unconfigurable processing steps (or steps whose configurability should not be
 * tracked) are most commonly represented by {@link CompositeSource}. Note that a step might also
 * include (interim) results and might thus be considered a result too.
 * <li>A result (e.g., {@link VariationDiff}) contains some data and the {@link getSources source of
 * that data}. The contained data is not directly accessible by {@link Source this interface} but
 * can be obtained by looking up the subclass representing that result with {@link findFirst} or
 * {@link findAll}. Results are not necessary to trace the source of some data (and it might thus be
 * omitted from the {@link Source} hierarchy) but can help to identify a buggy processing step.
 * </ul>
 * <p>
 * In summary, each {@link Source} in the {@link getSources hierarchy} has a {@link
 * getSourceExplanation short explanation} and a number of {@link getSourceArguments arguments}. A
 * source may contain additional data (e.g., the diff a variation diff originated from) that is too
 * big to be printed by the standard formatting functions ({@link shallowExplanation}, {@link
 * shortExplanation}, {@link fullExplanation}) of this interface. To access such data, or the
 * arguments of specific subclasses, you can use {@link findFirst} and {@link findAll}.
 * <p>
 * For dealing with generic sources, it is recommended to use the static methods of this interface.
 * They provide a more readable interface (e.g., {@code variationDiff.shallowExplanation()} vs.
 * {@code Source.shallowExplanation(variationDiff)}) and correctly deal with {@code null} sources
 * (treated as {@link Unknown}).
 *
 * @see CompositeSource
 * @see FileSource
 */
public interface Source {
    /**
     * Returns a short, one line explanation or identifier of this source.
     * The result is used by {@link shallowExplanation} and is formatted together with {@link
     * getSourceArguments} like {@code "sourceExplanation(argument1, argument2)"}.
     */
    String getSourceExplanation();

    /**
     * Returns a list of arguments required to understand this source.
     * Each argument in the result list should represent a {@link Object#toString string} without
     * newlines and will be formatted together with {@link getSourceExplanation} like {@code
     * "sourceExplanation(argument1, argument2)"} by {@link shallowExplanation}. This method is only
     * intended to access the arguments for {@link Object#toString printing}. In case access to the
     * well-typed object is required, use {@link findFirst} or {@link findAll} and use the accessors
     * of the underlying type.
     * <p>
     * For ease of implementing {@code Source}, the return value is a list of {@link Object}s
     * instead of a list of {@link String}s to allow code like {@code List.of(arg0, arg1, arg2)}
     * instead of {@code List.of(arg0.toString(), arg1.toString(), arg2.toString())}. Users of this
     * function should assume nothing from the returned objects except that {@link Object#toString}
     * is well behaved.
     *
     * @return an empty list by default
     * @see shallowExplanation
     */
    default List<Object> getSourceArguments() {
        return Collections.emptyList();
    }

    /**
     * Returns a list of sources that influenced this source.
     * Noteworthy processing steps and incorporation of multiple sources are implemented as a tree
     * of sources. This functions returns the children of this source.
     * <p>
     * By default, the first child source is treated specially in {@link getRootSource}.
     *
     * @return an empty list by default
     */
    default List<Source> getSources() {
        return Collections.emptyList();
    }

    /**
     * An explanation of this source disregarding all {@link getSources child sources}.
     * The resulting string should not contain any newlines.
     *
     * @return {@code "sourceExplanation(argument1, argument2, ...)"} by default
     * @see getSourceExplanation
     * @see getSourceArguments
     */
    default String shallowExplanation() {
        return getSourceArguments()
            .stream()
            .map(Object::toString)
            .collect(Collectors.joining(", ", getSourceExplanation() + "(", ")"));
    }

    /**
     * Calls {@link shallowExplanation} on {@code source} but handles {@code null} like {@link
     * Unknown}.
     */
    static String shallowExplanation(Source source) {
        return (source == null ? Unknown : source).shallowExplanation();
    }

    /**
     * Returns one representative {@link Source} that identifies the initial data.
     * A good root source should help humans to understand at what data they are looking at. Good
     * examples is a filename or a commit and repository combination. Note that sometimes this is an
     * arbitrary decision (e.g., the state before or after a diff) and might depend on the problem
     * that is currently worked on. Hence, there is might not be one correct result in all
     * circumstances.
     *
     * @return the {@link getSources first child} or {@code this} if there are no {@link getSources children}
     */
    default Source getRootSource() {
        for (var source : getSources()) {
            if (source != null) {
                return source.getRootSource();
            }
        }

        return this;
    }

    /**
     * Returns a {@link shallowExplanation} of {@link getRootSource the root source} of {@code source}.
     */
    static String rootExplanation(Source source) {
        return (source == null ? Unknown : source.getRootSource())
            .shallowExplanation();
    }

    /**
     * Returns a short one line explanation of {@code source} by pairing {@link shortExplanation}
     * with {@link rootExplanation}.
     */
    static String shortExplanation(Source source) {
        if (source == null) {
            return Unknown.shallowExplanation();
        }

        Source root = source.getRootSource();
        if (root == source) {
            return source.shallowExplanation();
        } else {
            return source.shallowExplanation() + " of " + root.shallowExplanation();
        }
    }

    /**
     * Explains {@code this} source hierarchy in detail.
     * The {@code result} will contain at least one line per {@link Source} in the {@link getSources
     * hierarchy} (by default in the {@link shallowExplanation} format). The state of {@code result}
     * must be in a new line before and after a call to this method.
     *
     * @param level the current indentation level (two spaces per level) that needs to be added
     * before each line
     * @param result the string containing the result of this method
     */
    default void fullExplanation(int level, StringBuilder result) {
        for (int i = 0; i < level; ++i) {
            result.append("  ");
        }
        result.append(shallowExplanation(this));
        result.append("\n");

        for (var child : getSources()) {
            if (child == null) {
                Unknown.fullExplanation(level + 1, result);
            } else {
                child.fullExplanation(level + 1, result);
            }
        }
    }

    /**
     * Calls {@link fullExplanation(int, StringBuilder)} on {@code source} but handles {@code null}
     * like {@link Unknown} and returns the resulting string.
     */
    static String fullExplanation(Source source) {
        if (source == null) {
            return fullExplanation(Unknown);
        }

        var result = new StringBuilder();
        source.fullExplanation(0, result);
        return result.toString();
    }

    /**
     * Uses a depth first traversal of the {@link Source} {@link getSources hierarchy} to find and
     * return all instances of {@code clazz}.
     * This method is intended to provide access to the data contained in the source hierarchy. For
     * example, {@code Source.findAll(src, File.class)} finds all source files. Note that arguments
     * are not inspected. In case an implementation {@link Source} contains a {@link Path file} as
     * {@link getSourceArguments argument} it is not included in the result.
     *
     * @see findFirst
     */
    static <T> List<T> findAll(Source source, Class<T> clazz) {
        var result = new ArrayList<T>();
        findAll(result, source, clazz);
        return result;
    }

    /**
     * Implementation of {@link findAll(Source, Class)} using an {@code result} accumulator.
     */
    private static <T> void findAll(List<T> result, Source source, Class<T> clazz) {
        if (source == null) {
            return;
        }

        if (source.getClass() == clazz) {
            result.add(Cast.unchecked(source));
        }

        for (var child : source.getSources()) {
            findAll(result, child, clazz);
        }
    }

    /**
     * Uses a depth first traversal of the {@link Source} {@link getSources hierarchy} to find and
     * return the first instances of {@code clazz}.
     * This method is intended to provide access to the data contained in the source hierarchy. For
     * example, {@code Source.findFirst(src, PatchDiff.class)} finds the first {@link PatchDiff}.
     * This allows access to data and arguments like {@link PatchDiff#getDiff} but it does not
     * traverse the {@link getSourceArguments} directly.
     *
     * @see findAll
     */
    static <T> T findFirst(Source source, Class<T> clazz) {
        if (source == null) {
            return null;
        }

        if (source.getClass() == clazz) {
            return Cast.unchecked(source);
        }

        for (var child : source.getSources()) {
            var result = findFirst(child, clazz);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * A placeholder for an unknown source without any explanation.
     * Should be preferred to a {@code null} {@link Source} to make it easily grepable.
     */
    public static Source Unknown = new CompositeSource("Unknown");
}

package org.variantsync.diffdetective.diff.result;

import java.util.Arrays;
import java.util.Optional;

/**
 * Describes an error that occurred when processing a text-based diff.
 */
public enum DiffError {
    /**
     * A commit has no parents and thus no real diff.
     */
    COMMIT_HAS_NO_PARENTS("commit has no parents"),

    /**
     * Some internal error occurred when operating JGit.
     * The error is not further specified or unknown.
     */
    JGIT_ERROR("error when operating jgit"),

    /**
     * The patch of a file was requested, but the file was not found in a git diff.
     */
    FILE_NOT_FOUND("couldn't find the requested file or the requested file is unmodified"),

    /**
     * An error which occurred when obtaining the full diff from a local diff.
     */
    COULD_NOT_OBTAIN_FULLDIFF("could not obtain full diff"),

    /**
     * A preprocessor block was opened but not closed.
     * Typically, this occurs when an #endif is missing.
     */
    NOT_ALL_ANNOTATIONS_CLOSED("not all annotations closed"),

    /**
     * A file or patch contained an expression that closes an annotation block (typically an #endif)
     * but there is no block to close.
     */
    ENDIF_WITHOUT_IF("#endif without #if"),

    /**
     * A multiline macro was defined within a multiline macro.
     */
    MLMACRO_WITHIN_MLMACRO("definition of multiline macro within multiline macro"),

    /**
     * An #else or #elif expression has no corresponding #if (or #ifdef, ...) expression.
     */
    ELSE_OR_ELIF_WITHOUT_IF("#else or #elif without #if"),

    /**
     * An #else expression is followed by another #else expression which has no semantics.
     */
    ELSE_AFTER_ELSE("#else after #else"),

    /**
     * A condition annotation is missing an expression.
     * This typically occurs when an #if macro has no arguments.
     */
    IF_WITHOUT_CONDITION("conditional macro without expression"),

    /**
     * Unknown macro name which was identified as a conditional macro.
     * Example: {@code #iflol}
     */
    INVALID_MACRO_NAME("invalid preprocessor macro name"),

    /**
     * Empty line in a diff.
     * All lines in a diff need at least one symbol (the
     * {@link org.variantsync.diffdetective.diff.difftree.DiffType}) in it.
     */
    INVALID_DIFF("missing diff symbol"),

    /**
     * A line continuation without a following line.
     */
    INVALID_LINE_CONTINUATION("a line continuation was detected but there are no more lines");

    private final String message;

    private DiffError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static Optional<DiffError> fromMessage(String message) {
        return Arrays
            .stream(values())
            .filter(m -> m.getMessage().equals(message))
            .findFirst();
    }

    @Override
    public String toString() {
        return message;
    }
}

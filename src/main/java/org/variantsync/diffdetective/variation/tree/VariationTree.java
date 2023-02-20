package org.variantsync.diffdetective.variation.tree;

import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.NodeType; // For Javadoc
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.diffdetective.variation.tree.source.LocalFileSource;
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Representation of a concrete variation tree with source information.
 *
 * @param root the root of the variation tree
 * @param source from which source code the variation tree was obtained
 * @see VariationTreeNode
 * @author Benjamin Moosherr
 */
public record VariationTree(
    VariationTreeNode root,
    VariationTreeSource source
) {
    /** Creates a {@code VariationTree} with the given root and an unknown source. */
    public VariationTree(VariationTreeNode root) {
        this(root, VariationTreeSource.Unknown);
    }

    /** Creates a {@code VariationTree} with the given root and source. */
    public VariationTree(VariationTreeNode root, VariationTreeSource source) {
        this.root = root;
        this.source = source;

        Assert.assertTrue(root.isRoot());
    }

    /**
     * Same as {@link fromFile(Path, boolean, CPPAnnotationParser)} but with
     * a {@link CPPAnnotationParser#Default default annotation parser}.
     */
    public static VariationTree fromFile(
        final Path p,
        final boolean collapseMultipleCodeLines
    ) throws IOException, DiffParseException {
        return fromFile(p, collapseMultipleCodeLines, CPPAnnotationParser.Default);
    }

    /**
     * Same as {@link fromFile(BufferedReader, VariationTreeSource, boolean, CPPAnnotationParser)}
     * but registers {@code path} as source.
     */
    public static VariationTree fromFile(
        final Path path,
        final boolean collapseMultipleCodeLines,
        final CPPAnnotationParser annotationParser
    ) throws IOException, DiffParseException {
        try (BufferedReader file = Files.newBufferedReader(path)) {
            return fromFile(
                file,
                new LocalFileSource(path),
                collapseMultipleCodeLines,
                annotationParser
            );
        }
    }

    /**
     * Same as {@link fromFile(BufferedReader, VariationTreeSource, boolean, CPPAnnotationParser)}
     * but with a {@link CPPAnnotationParser#Default default annotation parser}.
     */
    public static VariationTree fromFile(
        final BufferedReader input,
        final VariationTreeSource source,
        final boolean collapseMultipleCodeLines
    ) throws IOException, DiffParseException {
        return fromFile(
            input,
            source,
            collapseMultipleCodeLines,
            CPPAnnotationParser.Default
        );
    }

    /**
     * Parses a {@code VariationTree} from source code with C preprocessor annotations.
     *
     * @param input the source code to be parsed
     * @param collapseMultipleCodeLines Set to true if subsequent lines of source code with the same
     * {@link NodeType type} should be collapsed into a single artifact node representing all lines
     * at once.
     * @param annotationParser the parser that is used to parse preprocessor expressions
     * @return a new {@code VariationTree} representing {@code input}
     * @throws IOException if {@code input} throws {@code IOException}
     * @throws DiffParseException if some preprocessor annotations can't be parsed
     */
    public static VariationTree fromFile(
        final BufferedReader input,
        final VariationTreeSource source,
        final boolean collapseMultipleCodeLines,
        final CPPAnnotationParser annotationParser
    ) throws IOException, DiffParseException {
        VariationTreeNode tree = DiffTreeParser
            .createVariationTree(input, collapseMultipleCodeLines, false, annotationParser)
            .getRoot()
            // Arbitrarily choose the BEFORE projection as both should be equal.
            .projection(BEFORE)
            .toVariationTree();

        return new VariationTree(tree, source);
    }

    private void forAll(final VariationTreeNode v, final Consumer<VariationTreeNode> procedure) {
        procedure.accept(v);
        for (final VariationTreeNode c : v.getChildren()) {
            forAll(c, procedure);
        }
    }

    /**
     * Invokes the given callback for each node in this Variation Tree in depth-first order.
     * @param procedure callback
     * @return this
     */
    public VariationTree forAll(final Consumer<VariationTreeNode> procedure) {
        forAll(root, procedure);
        return this;
    }

    @Override
    public String toString() {
        return "variation tree from " + source;
    }
}

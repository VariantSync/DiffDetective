package org.variantsync.diffdetective.variation.tree;

import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.NodeType; // For Javadoc
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Projection;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.diffdetective.variation.diff.source.FromVariationTreeSource;
import org.variantsync.diffdetective.variation.tree.source.LocalFileSource;
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

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
     * Same as {@link #fromFile(BufferedReader, VariationTreeSource, DiffTreeParseOptions)}
     * but registers {@code path} as source.
     */
    public static VariationTree fromFile(
        final Path path,
        final DiffTreeParseOptions parseOptions
    ) throws IOException, DiffParseException {
        try (BufferedReader file = Files.newBufferedReader(path)) {
            return fromFile(
                file,
                new LocalFileSource(path),
                    parseOptions
            );
        }
    }

    /**
     * Parses a {@code VariationTree} from source code with C preprocessor annotations.
     *
     * @param input the source code to be parsed
     * @param parseOptions {@link PatchDiffParseOptions} for the parsing process.
     * @return a new {@code VariationTree} representing {@code input}
     * @throws IOException if {@code input} throws {@code IOException}
     * @throws DiffParseException if some preprocessor annotations can't be parsed
     */
    public static VariationTree fromFile(
            final BufferedReader input,
            final VariationTreeSource source,
            final DiffTreeParseOptions parseOptions
            ) throws IOException, DiffParseException {
        VariationTreeNode tree = DiffTreeParser
            .createVariationTree(input, parseOptions)
            .getRoot()
            // Arbitrarily choose the BEFORE projection as both should be equal.
            .projection(BEFORE)
            .toVariationTree();

        return new VariationTree(tree, source);
    }

    public static VariationTree fromProjection(final Projection projection, final VariationTreeSource source) {
        return fromVariationNode(projection, source);
    }

    public static <T extends VariationNode<T>> VariationTree fromVariationNode(final VariationNode<T> node, final VariationTreeSource source) {
        return new VariationTree(
                node.toVariationTree(),
                source
        );
    }

    public DiffTree toDiffTree(final Function<VariationTreeNode, DiffNode> nodeConverter) {
        return new DiffTree(
                DiffNode.unchanged(nodeConverter, root()),
                new FromVariationTreeSource(source())
        );
    }

    public DiffTree toCompletelyUnchangedDiffTree() {
        return toDiffTree(DiffNode::unchangedFlat);
    }

    /**
     * Invokes the given callback for each node in this Variation Tree in depth-first order.
     * @param action callback
     * @return this
     */
    public VariationTree forAllPreorder(final Consumer<VariationTreeNode> action) {
        root.forAllPreorder(action);
        return this;
    }


    /**
     * Returns the number of nodes in this Variation Tree.
     */
    public int computeSize() {
        AtomicInteger size = new AtomicInteger();
        forAllPreorder(n -> size.incrementAndGet());
        return size.get();
    }

    public VariationTree deepCopy() {
        return deepCopy(new HashMap<>());
    }

    /**
     * Creates a deep copy of this variation tree.
     *
     * <p>The map {@code oldToNew} should be empty as it will be filled by this method. After the
     * method call, the map keys will contain all nodes in this tree. The corresponding values will
     * be the nodes in the returned tree, where each pair (k, v) denotes that v was cloned from k.
     *
     * @param oldToNew A map that memorizes the translation of individual nodes.
     * @return A deep copy of this tree.
     */
    public VariationTree deepCopy(final Map<VariationTreeNode, VariationTreeNode> oldToNew) {
        return new VariationTree(root.deepCopy(oldToNew), this.source);
    }

    @Override
    public String toString() {
        return "variation tree from " + source;
    }
}

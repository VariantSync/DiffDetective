package org.variantsync.diffdetective.variation.diff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.tree.VariationTree;

/**
 * A {@link VariabilityAwareDiffer} that first {@link VariationTree#fromFile parses} {@link VariationTree}s and
 * diffs these trees.
 */
@FunctionalInterface
public interface VariabilityAwareTreeDiffer<L extends Label> extends VariabilityAwareDiffer<L> {
    /**
     * Creates a {@link VariationDiff} by diffing two variation trees.
     */
    VariationDiff<L> diffTrees(VariationTree<DiffLinesLabel> before, VariationTree<DiffLinesLabel> after);

    /**
     * Returns the options used for parsing the {@link VariationTree}s in {@link parseTree}.
     */
    default VariationDiffParseOptions getParseOptions() {
        return VariationDiffParseOptions.Default;
    }

    /**
     * {@link VariationTree#fromFile Parses} {@code input} into a {@link VariationTree} using
     * the options from {@code getParseOptions}.
     */
    default VariationTree<DiffLinesLabel> parseTree(Reader input, Source source) throws IOException, DiffParseException {
        return VariationTree.fromFile(new BufferedReader(input), source, getParseOptions());
    }

    /**
     * Composes {@link parseTree} and {@link diffTrees} to create a {@link VariationDiff}.
     */
    @Override
    default VariationDiff<L> diff(Reader before, Reader after, Source beforeSource, Source afterSource) throws IOException, DiffParseException {
        return diffTrees(
            parseTree(before, beforeSource),
            parseTree(after, afterSource)
        );
    }
}

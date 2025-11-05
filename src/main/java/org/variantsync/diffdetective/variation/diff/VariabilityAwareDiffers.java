package org.variantsync.diffdetective.variation.diff;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.CompositeSource;
import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.construction.GumTreeDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser; // For Javadoc
import org.variantsync.diffdetective.variation.tree.VariationTree;

import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;

/**
 * A collection of differs that create variation diffs.
 * @see VariabilityAwareDiffer
 * @see VariationDiff
 */
public class VariabilityAwareDiffers {
    /**
     * Returns a differ that {@link VariationDiffParser#createVariationDiff parses} a
     * {@link VariationDiff} after {@link JGitDiff#textDiff diffing} with
     * {@link DiffAlgorithm.SupportedAlgorithm one of JGit's differ}.
     *
     * @param lineDiffAlgorithm the Git diffing algorithm to use
     * @param parseOptions options for parsing the {@link VariationDiff}
     * @return the diffed {@link VariationDiff}
     */
    public static VariabilityAwareTextDiffer JGit(DiffAlgorithm.SupportedAlgorithm lineDiffAlgorithm, VariationDiffParseOptions parseOptions) {
        return new VariabilityAwareTextDiffer() {
            @Override
            public String diffLines(Reader before, Reader after) throws IOException {
                return JGitDiff.textDiff(IOUtils.toString(before), IOUtils.toString(after), lineDiffAlgorithm);
            }

            @Override
            public String diffLines(String before, String after) throws IOException {
                return JGitDiff.textDiff(before, after, lineDiffAlgorithm);
            }

            @Override
            public VariationDiffParseOptions getParseOptions() {
                return parseOptions;
            }
        };
    }

    /**
     * Calls {@link JGit(DiffAlgorithm.SupportedAlgorithm, VariationDiffParseOptions)} with
     * {@link DiffAlgorithm.SupportedAlgorithm#MYERS} and {@link VariationDiffParseOptions#Default}.
     */
    public static VariabilityAwareTextDiffer JGitMyers() {
        return JGit(DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default);
    }

    /**
     * Returns a differ that {@link VariationTree#fromFile parses} two variation trees and then
     * {@link GumTreeDiff#diffUsingMatching diffs} these variation trees.
     *
     * @param parseOptions options for parsing the {@link VariationTree}s
     * @param matcher the matcher used for diffing the {@link VariationTree}s
     * @return the diffed {@link VariationDiff}
     */
    public static VariabilityAwareTreeDiffer<DiffLinesLabel> GumTree(VariationDiffParseOptions parseOptions, Matcher matcher) {
        return new VariabilityAwareTreeDiffer<DiffLinesLabel>() {
            @Override
            public VariationDiff<DiffLinesLabel> diffTrees(VariationTree<DiffLinesLabel> before, VariationTree<DiffLinesLabel> after) {
                return GumTreeDiff.diffUsingMatching(before, after, matcher);
            }

            @Override
            public VariationDiffParseOptions getParseOptions() {
                return parseOptions;
            }
        };
    }

    /**
     * Calls {@link GumTree(VariationDiffParseOptions, Matcher)} with {@link
     * VariationDiffParseOptions#Default} and {@link Matchers#getInstance {@code
     * Matchers.getInstance().getMatcher()}}.
     */
    public static VariabilityAwareTreeDiffer<DiffLinesLabel> GumTree() {
        return GumTree(VariationDiffParseOptions.Default, Matchers.getInstance().getMatcher());
    }

    /**
     * Returns a differ that first parses a {@link VariationDiff} like {@link JGit} and then
     * {@link GumTreeDiff#improveMatching improves} the represented matching.
     *
     * @param lineDiffAlgorithm the Git diffing algorithm to use
     * @param parseOptions options for parsing the {@link VariationDiff}
     * @param matcher the matcher used for diffing the variation trees
     * @return the diffed {@link VariationDiff}
     */
    public static VariabilityAwareTextDiffer JGitGumTreeHybrid(DiffAlgorithm.SupportedAlgorithm lineDiffAlgorithm, VariationDiffParseOptions parseOptions, Matcher matcher) {
        return new VariabilityAwareTextDiffer() {
            @Override
            public String diffLines(Reader before, Reader after) throws IOException {
                return JGitDiff.textDiff(IOUtils.toString(before), IOUtils.toString(after), lineDiffAlgorithm);
            }

            @Override
            public VariationDiffParseOptions getParseOptions() {
                return parseOptions;
            }

            @Override
            public VariationDiff<DiffLinesLabel> diff(Reader before, Reader after, Source beforeSource, Source afterSource) throws IOException, DiffParseException {
                VariationDiff<DiffLinesLabel> vd = VariabilityAwareTextDiffer.super.diff(before, after, beforeSource, afterSource);
                return new VariationDiff<>(
                    GumTreeDiff.improveMatching(vd.getRoot(), matcher),
                    new CompositeSource("GumTreeDiff.improveMatching", vd.getSource())
                );
            };
        };
    }

    /**
     * Calls {@link JGitGumTreeHybrid(DiffAlgorithm.SupportedAlgorithm, VariationDiffParseOptions,
     * Matcher)} with {@link DiffAlgorithm.SupportedAlgorithm#MYERS}, {@link
     * VariationDiffParseOptions#Default}, and {@link Matchers#getInstance {@code
     * Matchers.getInstance().getMatcher()}}.
     */
    public static VariabilityAwareTextDiffer JGitMyersGumTreeHybrid() {
        return JGitGumTreeHybrid(DiffAlgorithm.SupportedAlgorithm.MYERS, VariationDiffParseOptions.Default, Matchers.getInstance().getMatcher());
    }
}

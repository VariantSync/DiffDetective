package org.variantsync.diffdetective.diff.difftree.serialize;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.functjonal.Pair;

/**
 * Class that contains functions for writing {@link CommitDiff}s and (sets of) {@link DiffTree}s to a linegraph file.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public final class LineGraphExport {
    private LineGraphExport() {}

    /**
     * Exports the given DiffTree to a linegraph String. No file will be written.
     * @param diffTree The difftree to export to linegraph format.
     * @param options Configuration options for the export, such as the format used for node and edge labels.
     * @return A pair holding some debug information and the produced linegraph as a string.
     */
    public static Pair<DiffTreeSerializeDebugData, String> toLineGraphFormat(final DiffTree diffTree, final DiffTreeLineGraphExportOptions options) {
        DiffTreeTransformer.apply(options.treePreProcessing(), diffTree);
        diffTree.assertConsistency();

        if (options.treeFilter().test(diffTree)) {
            final DiffTreeLineGraphExporter exporter = new DiffTreeLineGraphExporter(diffTree);
            final String result = exporter.export(options);
            return new Pair<>(exporter.getDebugData(), result);
        }

        return null;
    }

    /**
     * Exports the given DiffTrees that originated from a repository with the given name.
     * @param repoName The name of the repository, the given DiffTrees originated from.
     * @param trees The set of trees to export.
     * @param options Configuration options for the export, such as the format used for node and edge labels.
     * @return A pair of (1) metadata about the exported DiffTrees, and (2) the produced linegraph as String.
     */
    public static Pair<AnalysisResult, String> toLineGraphFormat(final String repoName, final Iterable<DiffTree> trees, final DiffTreeLineGraphExportOptions options) {
        final AnalysisResult result = new AnalysisResult(repoName);

        final StringBuilder lineGraph = new StringBuilder();
        for (final DiffTree t : trees) {
            final Pair<DiffTreeSerializeDebugData, String> lg = toLineGraphFormat(t, options);

            if (lg != null) {
                result.debugData.append(lg.first());
                composeTreeInLineGraph(lineGraph, t.getSource(), lg.second(), options);
                ++result.exportedTrees;
            }
        }

        result.exportedCommits = 1;
        result.filterHits = new ExplainedFilterSummary(options.treeFilter());

        return new Pair<>(result, lineGraph.toString());
    }

    /**
     * Same as {@link LineGraphExport#toLineGraphFormat(String, Iterable, DiffTreeLineGraphExportOptions)} but with an
     * {@link AnalysisResult#NO_REPO unkown repository}.
     */
    public static Pair<AnalysisResult, String> toLineGraphFormat(final Iterable<DiffTree> trees, final DiffTreeLineGraphExportOptions options) {
        return toLineGraphFormat(AnalysisResult.NO_REPO, trees, options);
    }

    /**
     * Same as {@link LineGraphExport#toLineGraphFormat(String, Iterable, DiffTreeLineGraphExportOptions)} but with an
     * {@link AnalysisResult#NO_REPO unkown repository}.
     */
    public static AnalysisResult toLineGraphFormat(final CommitDiff commitDiff, final StringBuilder lineGraph, final DiffTreeLineGraphExportOptions options) {
        return toLineGraphFormat(AnalysisResult.NO_REPO, commitDiff, lineGraph, options);
    }

    /**
     * Writes the given commitDiff in linegraph format to the given StringBuilder.
     * @param repoName The name of the repository from which the given CommitDiff originated.
     * @param commitDiff The diff to convert to line graph format.
     * @param lineGraph The string builder to write the result to.
     * @param options Configuration options for the export, such as the format used for node and edge labels.
     * @return The number of the next diff tree to export (updated value of treeCounter).
     */
    public static AnalysisResult toLineGraphFormat(final String repoName, final CommitDiff commitDiff, final StringBuilder lineGraph, final DiffTreeLineGraphExportOptions options) {
        final AnalysisResult result = new AnalysisResult(repoName);

        final String hash = commitDiff.getCommitHash();
        for (final PatchDiff patchDiff : commitDiff.getPatchDiffs()) {
            if (patchDiff.isValid()) {
                //Logger.info("  Exporting DiffTree #{}", treeCounter);
                final Pair<DiffTreeSerializeDebugData, String> patchDiffLg;
                try {
                    patchDiffLg = toLineGraphFormat(patchDiff.getDiffTree(), options);
                } catch (Exception e) {
                    options.onError().accept(patchDiff, e);
                    break;
                }

                if (patchDiffLg != null) {
                    result.debugData.append(patchDiffLg.first());
                    composeTreeInLineGraph(lineGraph, patchDiff, patchDiffLg.second(), options);
                    ++result.exportedTrees;
                }
            } else {
                Logger.debug("  Skipping invalid patch for file {} at commit {}", patchDiff.getFileName(), hash);
            }
        }

        result.exportedCommits = 1;
        result.filterHits = new ExplainedFilterSummary(options.treeFilter());

        return result;
    }
    
    /**
     * Produces the final linegraph file content.
     * Creates a linegraph header from the given DiffTreeSource using the {@link DiffTreeLineGraphExportOptions#treeFormat} in the given options.
     * Then appends the already created file content for nodes and edges.
     * @param lineGraph The string builder to write the result to.
     * @param source The {@link DiffTreeSource} that describes where the DiffTree whose content is written to the file originated from.
     * @param nodesAndEdges Result from {@link #toLineGraphFormat(DiffTree, DiffTreeLineGraphExportOptions)}. Holds all nodes and edges in linegraph format, separated by a newline each.
     * @param options {@link DiffTreeLineGraphExportOptions} used to determine the treeFormat for the header.
     */
    public static void composeTreeInLineGraph(final StringBuilder lineGraph, final DiffTreeSource source, final String nodesAndEdges, final DiffTreeLineGraphExportOptions options) {
    	lineGraph
    		.append(options.treeFormat().toLineGraphLine(source)) // print "t # $LABEL"
    		.append(StringUtils.LINEBREAK)
    		.append(nodesAndEdges)
    		.append(StringUtils.LINEBREAK)
    		.append(StringUtils.LINEBREAK);
    }
}

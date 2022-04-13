package diff.difftree.serialize;

import diff.CommitDiff;
import diff.PatchDiff;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.DiffTreeSource;
import diff.difftree.transform.DiffTreeTransformer;
import metadata.ExplainedFilterSummary;
import mining.DiffTreeMiningResult;
import org.tinylog.Logger;
import org.variantsync.functjonal.Pair;
import util.StringUtils;

public class LineGraphExport {

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

    public static Pair<DiffTreeMiningResult, String> toLineGraphFormat(final String repoName, final Iterable<DiffTree> trees, final DiffTreeLineGraphExportOptions options) {
        final DiffTreeMiningResult result = new DiffTreeMiningResult(repoName);

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

    public static Pair<DiffTreeMiningResult, String> toLineGraphFormat(final Iterable<DiffTree> trees, final DiffTreeLineGraphExportOptions options) {
        return toLineGraphFormat(DiffTreeMiningResult.NO_REPO, trees, options);
    }

    public static DiffTreeMiningResult toLineGraphFormat(final CommitDiff commitDiff, final StringBuilder lineGraph, final DiffTreeLineGraphExportOptions options) {
        return toLineGraphFormat(DiffTreeMiningResult.NO_REPO, commitDiff, lineGraph, options);
    }

        /**
         * Writes the given commitDiff in line graph format to the given StringBuilder.
         * @param commitDiff The diff to convert to line graph format.
         * @param lineGraph The string builder to write the result to.
         * @return The number of the next diff tree to export (updated value of treeCounter).
         */
    public static DiffTreeMiningResult toLineGraphFormat(final String repoName, final CommitDiff commitDiff, final StringBuilder lineGraph, final DiffTreeLineGraphExportOptions options) {
        final DiffTreeMiningResult result = new DiffTreeMiningResult(repoName);

        final String hash = commitDiff.getCommitHash();
        for (final PatchDiff patchDiff : commitDiff.getPatchDiffs()) {
            if (patchDiff.isValid()) {
                //Logger.info("  Exporting DiffTree #" + treeCounter);
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
                Logger.debug("  Skipping invalid patch for file " + patchDiff.getFileName() + " at commit " + hash);
            }
        }

        result.exportedCommits = 1;
        result.filterHits = new ExplainedFilterSummary(options.treeFilter());

        return result;
    }
    
    /**
     * Compose a tree from a {@link DiffTree} with its {@link DiffNode DiffNodes} and edges.
     * 
     * @param lineGraph The string builder to write the result to
     * @param source {@link DiffTreeSource}
     * @param nodesAndEdges Result from {@link #toLineGraphFormat(DiffTree, DiffTreeLineGraphExportOptions)}
     * @param options {@link DiffTreeLineGraphExportOptions}
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
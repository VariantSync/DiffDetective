package diff.difftree.serialize;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.PatchDiff;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.DiffTreeSource;
import diff.difftree.transform.DiffTreeTransformer;
import org.pmw.tinylog.Logger;
import util.StringUtils;

public class LineGraphExport {

    public static Pair<DiffTreeSerializeDebugData, String> toLineGraphFormat(final DiffTree diffTree, final DiffTreeLineGraphExportOptions options) {
        DiffTreeTransformer.apply(options.treePreProcessing(), diffTree);
        diffTree.assertConsistency();

        if (options.treeFilter().test(diffTree)) {
            final DiffTreeLineGraphExporter exporter = new DiffTreeLineGraphExporter(diffTree);
            final String result = exporter.export(options);
            return new Pair<>(exporter.getDebugData(), result);
        } else {
            return new Pair<>(new DiffTreeSerializeDebugData(), "");
        }
    }

    /**
     * Writes the given commitDiff in line graph format to the given StringBuilder.
     * @param commitDiff The diff to convert to line graph format.
     * @param lineGraph The string builder to write the result to.
     * @return The number of the next diff tree to export (updated value of treeCounter).
     */
    public static Pair<DiffTreeSerializeDebugData, Integer> toLineGraphFormat(final CommitDiff commitDiff, final StringBuilder lineGraph, final DiffTreeLineGraphExportOptions options) {
        final DiffTreeSerializeDebugData debugData = new DiffTreeSerializeDebugData();

        int exportedTrees = 0;

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

                debugData.mappend(patchDiffLg.getKey());

                if (!patchDiffLg.getValue().isEmpty()) {
                    composeTreeInLineGraph(lineGraph, patchDiff, patchDiffLg.getValue(), options);
                    ++exportedTrees;
                }
            } else {
                Logger.debug("  Skipping invalid patch for file " + patchDiff.getFileName() + " at commit " + hash);
            }
        }

        return new Pair<>(debugData, exportedTrees);
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
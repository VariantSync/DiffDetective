package diff.serialize;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.difftree.DiffTree;
import diff.PatchDiff;
import diff.difftree.serialize.DiffTreeLineGraphExporter;
import diff.difftree.transform.DiffTreeTransformer;
import org.pmw.tinylog.Logger;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LineGraphExport {
    public static final String TREE_NAME_SEPARATOR = "$$$";

    public enum NodePrintStyle {
        /// Print CodeType and DiffType
        Type,
        /// Print Node as Code
        Pretty,
        /// Print CodeType and DiffType and Mappings of Macros
        Mappings,
        /// Print CodeType and DiffType and Mappings if Macro and Text if Code
        Verbose
    }
    public static record Options(
            NodePrintStyle nodePrintStyle,
            boolean skipEmptyTrees,
            List<DiffTreeTransformer> treePreProcessing) {
        public Options(NodePrintStyle nodePrintStyle) {
            this(nodePrintStyle, false, new ArrayList<>());
        }
    }

    public static Pair<DiffTreeSerializeDebugData, String> toLineGraphFormat(final DiffTree diffTree, final Options options) {
        DiffTreeTransformer.apply(options.treePreProcessing, diffTree);

        if (!diffTree.isConsistent()) {
            throw new IllegalStateException(diffTree + " is inconsistent after transformation with " + options.treePreProcessing + "!");
        }

        if (options.skipEmptyTrees && diffTree.isEmpty()) {
            return new Pair<>(new DiffTreeSerializeDebugData(), "");
        }

        final DiffTreeLineGraphExporter exporter = new DiffTreeLineGraphExporter(diffTree);
        final String result = exporter.export(options);
        return new Pair<>(exporter.getDebugData(), result);
    }

    /**
     * Writes the given commitDiff in line graph format to the given StringBuilder.
     * @param commitDiff The diff to convert to line graph format.
     * @param lineGraph The string builder to write the result to.
     * @param treeCounter The number of the first diff tree to export.
     * @return The number of the next diff tree to export (updated value of treeCounter).
     */
    public static Pair<DiffTreeSerializeDebugData, Integer> toLineGraphFormat(final CommitDiff commitDiff, final StringBuilder lineGraph, int treeCounter, final Options options) {
        final DiffTreeSerializeDebugData debugData = new DiffTreeSerializeDebugData();

        final String hash = commitDiff.getCommitHash();
        for (final PatchDiff patchDiff : commitDiff.getPatchDiffs()) {
            if (patchDiff.isValid()) {
                //Logger.info("  Exporting DiffTree #" + treeCounter);
                final Pair<DiffTreeSerializeDebugData, String> patchDiffLg = toLineGraphFormat(patchDiff.getDiffTree(), options);
                debugData.mappend(patchDiffLg.getKey());

                if (!patchDiffLg.getValue().isEmpty()) {
                    lineGraph
//                        .append("t # ").append(treeCounter)
                            .append("t # ").append(patchDiff.getFileName()).append(TREE_NAME_SEPARATOR).append(hash)
                            .append(StringUtils.LINEBREAK)
                            .append(patchDiffLg.getValue())
                            .append(StringUtils.LINEBREAK)
                            .append(StringUtils.LINEBREAK);

                    ++treeCounter;
                }
            } else {
                Logger.info("  Skipping invalid patch for file " + patchDiff.getFileName() + " at commit " + hash);
            }
        }

        return new Pair<>(debugData, treeCounter);
    }
}

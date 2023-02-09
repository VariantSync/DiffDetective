package org.variantsync.diffdetective.variation.diff.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.MetadataKeys;
import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.functjonal.category.InplaceSemigroup;

/**
 * Class that contains functions for writing {@link CommitDiff}s and (sets of) {@link DiffTree}s to a linegraph file.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public final class LineGraphExport {
    private LineGraphExport() {}

    public static final ResultKey<Statistic> STATISTIC = new ResultKey<>("LineGraphExporter");
    public static final class Statistic implements Metadata<Statistic> {
        /**
        * The number of commits that were processed.
        * {@code exportedCommits <= totalCommits}
        */
        public int exportedCommits = 0;
        /**
        * Number of DiffTrees that were processed.
        */
        public int exportedTrees = 0;
        /**
         * Debug data for DiffTree serialization.
         */
        public final DiffTreeSerializeDebugData debugData = new DiffTreeSerializeDebugData();

        public static final InplaceSemigroup<Statistic> ISEMIGROUP = (a, b) -> {
            a.exportedCommits += b.exportedCommits;
            a.exportedTrees += b.exportedTrees;
            a.debugData.append(b.debugData);
        };

        @Override
        public InplaceSemigroup<Statistic> semigroup() {
            return ISEMIGROUP;
        }

        @Override
        public LinkedHashMap<String, Object> snapshot() {
            var snap = new LinkedHashMap<String, Object>();
            snap.put(MetadataKeys.EXPORTED_COMMITS, exportedCommits);
            snap.put(MetadataKeys.EXPORTED_TREES, exportedTrees);
            snap.putAll(debugData.snapshot());
            return snap;
        }

        @Override
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            exportedCommits = Integer.parseInt(snap.get(MetadataKeys.EXPORTED_COMMITS));
            exportedTrees = Integer.parseInt(snap.get(MetadataKeys.EXPORTED_TREES));
            debugData.setFromSnapshot(snap);
        }
    }

    /**
     * Exports the given DiffTree to a linegraph String. No file will be written.
     * @param diffTree The difftree to export to linegraph format.
     * @param options Configuration options for the export, such as the format used for node and edge labels.
     * @return A pair holding some debug information and the produced linegraph as a string.
     */
    public static DiffTreeSerializeDebugData toLineGraphFormat(final DiffTree diffTree, final LineGraphExportOptions options, OutputStream destination) throws IOException {
        final var exporter = new LineGraphExporter(options);
        exporter.exportDiffTree(diffTree, destination);
        return exporter.getDebugData();
    }

    /**
     * Exports the given DiffTrees that originated from a repository with the given name.
     * @param trees The set of trees to export.
     * @param options Configuration options for the export, such as the format used for node and edge labels.
     * @return A pair of (1) metadata about the exported DiffTrees, and (2) the produced linegraph as String.
     */
    public static Statistic toLineGraphFormat(final Iterable<DiffTree> trees, final LineGraphExportOptions options, OutputStream destination) throws IOException {
        final var result = new Statistic();

        for (final DiffTree t : trees) {
            destination.write(lineGraphHeader(t.getSource(), options).getBytes());
            result.debugData.append(toLineGraphFormat(t, options, destination));
            destination.write(lineGraphFooter().getBytes());
            ++result.exportedTrees;
        }

        result.exportedCommits = 1;

        return result;
    }

    /**
     * Writes the given commitDiff in linegraph format to the given StringBuilder.
     * @param commitDiff The diff to convert to line graph format.
     * @param options Configuration options for the export, such as the format used for node and edge labels.
     * @param destination where the resulting line graph is written
     * @return The number of the next diff tree to export (updated value of treeCounter).
     */
    public static Statistic toLineGraphFormat(final CommitDiff commitDiff, LineGraphExportOptions options, OutputStream destination) throws IOException {
        final var result = new Statistic();

        for (final PatchDiff patchDiff : commitDiff.getPatchDiffs()) {
            try {
                result.append(toLineGraphFormat(patchDiff, options, destination));
            } catch (Exception e) {
                options.onError().accept(patchDiff, e);
                break;
            }
        }

        result.exportedCommits = 1;

        return result;
    }

    /**
     * Writes the given patch in linegraph format to the given StringBuilder.
     * @param patch The diff to convert to line graph format.
     * @param options Configuration options for the export, such as the format used for node and edge labels.
     * @param destination where the resulting line graph is written
     * @return The number of the next diff tree to export (updated value of treeCounter).
     */
    public static Statistic toLineGraphFormat(final PatchDiff patch, final LineGraphExportOptions options, OutputStream destination) throws IOException {
        final var result = new Statistic();

        if (patch.isValid()) {
            //Logger.info("  Exporting DiffTree #{}", treeCounter);

            //Logger.info("  Exporting DiffTree #{}", treeCounter);
            destination.write(lineGraphHeader(patch, options).getBytes());
            result.debugData.append(toLineGraphFormat(patch.getDiffTree(), options, destination));
            destination.write(lineGraphFooter().getBytes());

            ++result.exportedTrees;
        } else {
            Logger.debug("  Skipping invalid patch for file {} at commit {}", patch.getFileName(), patch.getCommitHash());
        }

        return result;
    }

    /**
     * Produces the final linegraph file content.
     * Creates a linegraph header from the given DiffTreeSource using the {@link LineGraphExportOptions#treeFormat} in the given options.
     * Then appends the already created file content for nodes and edges.
     * @param lineGraph The string builder to write the result to.
     * @param source The {@link DiffTreeSource} that describes where the DiffTree whose content is written to the file originated from.
     * @param nodesAndEdges Result from {@link #toLineGraphFormat(DiffTree, LineGraphExportOptions, OutputStream)}. Holds all nodes and edges in linegraph format, separated by a newline each.
     * @param options {@link LineGraphExportOptions} used to determine the treeFormat for the header.
     */
    private static String lineGraphHeader(final DiffTreeSource source, final LineGraphExportOptions options) {
        return options.treeFormat().toLineGraphLine(source) + StringUtils.LINEBREAK;
    }

    private static String lineGraphFooter() {
        return StringUtils.LINEBREAK + StringUtils.LINEBREAK;
    }
}

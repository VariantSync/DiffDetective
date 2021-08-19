package util;

import diff.data.CommitDiff;
import diff.data.DiffTree;
import diff.data.PatchDiff;
import org.pmw.tinylog.Logger;

public class LineGraphExport {
    public static String toLineGraphFormat(final DiffTree diffTree) {
        return new DiffTreeLineGraphExporter(diffTree).export();
    }

    /**
     * Writes the given commitDiff in line graph format to the given StringBuilder.
     * @param commitDiff The diff to convert to line graph format.
     * @param lineGraph The string builder to write the result to.
     * @param treeCounter The number of the first diff tree to export.
     * @return The number of the next diff tree to export (updated value of treeCounter).
     */
    public static int toLineGraphFormat(final CommitDiff commitDiff, final StringBuilder lineGraph, int treeCounter) {
        final String hash = commitDiff.getCommitHash();
        for (final PatchDiff patchDiff : commitDiff.getPatchDiffs()) {
            if (patchDiff.isValid()) {
                //Logger.info("  Exporting DiffTree #" + treeCounter);
                lineGraph
//                        .append("t # ").append(treeCounter)
                        .append("t # ").append(patchDiff.getFileName()).append("$$$").append(hash)
                        .append(StringUtils.LINEBREAK)
                        .append(toLineGraphFormat(patchDiff.getDiffTree()))
                        .append(StringUtils.LINEBREAK)
                        .append(StringUtils.LINEBREAK);
                ++treeCounter;
            } else {
                Logger.info("  Skipping invalid patch for file " + patchDiff.getFileName() + " at commit " + hash);
            }
        }

        return treeCounter;
    }
}

package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;
import org.variantsync.diffdetective.util.CSV;

/**
 * Statistics for processing a patch in a commit.
 * @param patchDiff The diff of the processed patch.
 * @param editClassCount Count statistics for the edit class matched to the edits in the patch.
 * @author Paul Bittner
 */
public record PatchStatistics(
        PatchDiff patchDiff,
        EditClassCount editClassCount) implements CSV {
    /**
     * Creates empty patch statistics for the given catalogue of edit classes.
     * @param patch The patch to gather statistics for.
     * @param catalogue A catalogue of edit classes which should be used for classifying edits.
     */
    public PatchStatistics(final PatchDiff patch, final EditClassCatalogue catalogue) {
        this(patch, new EditClassCount(catalogue));
    }

    @Override
    public String toCSV(final String delimiter) {
        return patchDiff.getCommitHash() + delimiter + patchDiff.getFileName() + delimiter + editClassCount.toCSV(delimiter);
    }
}

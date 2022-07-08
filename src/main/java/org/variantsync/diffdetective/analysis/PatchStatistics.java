package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPatternCatalogue;
import org.variantsync.diffdetective.util.CSV;

/**
 * Statistics for processing a patch in a commit.
 * @param patchDiff The diff of the processed patch.
 * @param elementaryPatternCount Count statistics for the elementary edit patterns matched to the edits in the patch.
 * @author Paul Bittner
 */
public record PatchStatistics(
        PatchDiff patchDiff,
        ElementaryPatternCount elementaryPatternCount) implements CSV {
    /**
     * Creates empty patch statistics for the given catalogue of edit patterns.
     * @param patch The patch to gather statistics for.
     * @param catalogue A catalogue of elementary edit patterns which should be used for classifying edits.
     */
    public PatchStatistics(final PatchDiff patch, final ElementaryPatternCatalogue catalogue) {
        this(patch, new ElementaryPatternCount(catalogue));
    }

    @Override
    public String toCSV(final String delimiter) {
        return patchDiff.getCommitHash() + delimiter + patchDiff.getFileName() + delimiter + elementaryPatternCount.toCSV(delimiter);
    }
}

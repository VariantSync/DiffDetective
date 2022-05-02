package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPatternCatalogue;
import org.variantsync.diffdetective.util.CSV;

public record PatchStatistics(
        PatchDiff patchDiff,
        ElementaryPatternCount elementaryPatternCount) implements CSV {
    public PatchStatistics(final PatchDiff patch, final ElementaryPatternCatalogue catalogue) {
        this(patch, new ElementaryPatternCount(catalogue));
    }

    public String toCSV(final String delimiter) {
        return patchDiff.getCommitHash() + delimiter + patchDiff.getFileName() + delimiter + elementaryPatternCount.toCSV(delimiter);
    }
}

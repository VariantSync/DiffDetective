package org.variantsync.diffdetective.variation.diff.source;

import org.variantsync.diffdetective.diff.text.TextBasedDiff;
import org.variantsync.diffdetective.util.Source;

/**
 * Source for VariationDiffs that were created from a patch given as a String.
 * @param getDiff The patch as a String.
 */
public record PatchString(String getDiff) implements TextBasedDiff, Source {
    @Override
    public String getSourceExplanation() {
        return "Patch";
    }

    @Override
    public String toString() {
        return getSourceExplanation() + getDiff;
    }
}

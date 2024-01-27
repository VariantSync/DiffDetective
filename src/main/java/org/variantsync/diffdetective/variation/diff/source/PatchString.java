package org.variantsync.diffdetective.variation.diff.source;

import org.variantsync.diffdetective.diff.text.TextBasedDiff;

/**
 * Source for VariationDiffs that were created from a patch given as a String.
 * @param getDiff The patch as a String.
 */
public record PatchString(String getDiff) implements TextBasedDiff, VariationDiffSource {
    @Override
    public String toString() {
        return "from line-based diff " + getDiff;
    }
}

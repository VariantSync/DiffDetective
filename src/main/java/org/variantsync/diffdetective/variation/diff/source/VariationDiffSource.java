package org.variantsync.diffdetective.variation.diff.source;

/**
 * Describes or identifies that data a VariationDiff was created or parsed from.
 * This is typically a patch.
 */
public interface VariationDiffSource {
    /**
     * Constant to use when the source of a VariationDiff is unknown
     * or if it was created artificially.
     */
    VariationDiffSource Unknown = new VariationDiffSource() {
        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "Unknown VariationDiffSource";
        }
    };
}

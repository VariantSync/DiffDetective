package org.variantsync.diffdetective.variation.diff.serialize;

/**
 * Refers to the structure that is represented by a VariationDiff.
 * For some purposes, certain diff graphs might also be represented
 * as a VariationDiff with an artificial root.
 */
public enum GraphFormat {
    /**
     * A diffgraph has no explicit root.
     */
    DIFFGRAPH,
    /**
     * Default value. Describes a VariationDiff that does not model anything other than a VariationDiff.
     */
    VARIATION_DIFF
}

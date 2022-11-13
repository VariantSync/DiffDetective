package org.variantsync.diffdetective.variation.diff.source;

import org.variantsync.diffdetective.diff.TextBasedDiff;

/**
 * Source for DiffTrees that were created from a patch given as a String.
 * @param getDiff The patch as a String.
 */
public record PatchString(String getDiff) implements TextBasedDiff, DiffTreeSource { }

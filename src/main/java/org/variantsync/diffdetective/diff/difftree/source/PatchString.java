package org.variantsync.diffdetective.diff.difftree.source;

import org.variantsync.diffdetective.diff.TextBasedDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

/**
 * Source for DiffTrees that were created from a patch given as a String.
 * @param getDiff The patch as a String.
 */
public record PatchString(String getDiff) implements TextBasedDiff, DiffTreeSource { }

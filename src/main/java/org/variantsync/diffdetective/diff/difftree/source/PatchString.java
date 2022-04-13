package org.variantsync.diffdetective.diff.difftree.source;

import org.variantsync.diffdetective.diff.Diff;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

public record PatchString(String getDiff) implements Diff, DiffTreeSource { }

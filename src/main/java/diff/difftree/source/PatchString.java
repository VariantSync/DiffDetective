package diff.difftree.source;

import diff.Diff;
import diff.difftree.DiffTreeSource;

public record PatchString(String getDiff) implements Diff, DiffTreeSource { }

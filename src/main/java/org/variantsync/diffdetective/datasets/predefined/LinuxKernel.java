package org.variantsync.diffdetective.datasets.predefined;

import org.eclipse.jgit.diff.DiffEntry;
import org.variantsync.diffdetective.diff.git.DiffFilter;

/**
 * Default repository for the Linux Kernel.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
@Deprecated
public class LinuxKernel {
    @Deprecated
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
//            .blockedPaths(DiffEntry.DEV_NULL) // <- I'm with stupid.
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "h")
            .build();
}

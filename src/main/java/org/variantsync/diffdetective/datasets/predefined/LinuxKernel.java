package org.variantsync.diffdetective.datasets.predefined;

import org.eclipse.jgit.diff.DiffEntry;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.DiffFilter;
import org.variantsync.diffdetective.diff.difftree.DiffNode;

import java.nio.file.Path;

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

    /**
     * This may cause bugs.
     */
    @Deprecated
    public static boolean isFeature(DiffNode node) {
        return node.getLabel().contains("CONFIG_");
    }

    /**
     * Clones Linux from Github.
     * @param localDir Directory to clone the repository to.
     * @return Linux repository
     */
    @Deprecated
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/torvalds/linux", "Linux")
                .orElseThrow()
                .setDiffFilter(DIFF_FILTER)
//                .setFeatureAnnotationFilter(LinuxKernel::isFeature)
                ;
    }
}

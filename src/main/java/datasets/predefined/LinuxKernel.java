package datasets.predefined;

import datasets.Repository;
import diff.DiffFilter;
import diff.difftree.DiffNode;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;

/**
 * Default repository for the Linux Kernel.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class LinuxKernel {
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
//            .blockedPaths(DiffEntry.DEV_NULL) // <- I'm with stupid.
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "h")
            .build();

    /**
     * This can cause bugs.
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
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/torvalds/linux", "Linux")
                .orElseThrow()
                .setDiffFilter(DIFF_FILTER)
//                .setFeatureAnnotationFilter(LinuxKernel::isFeature)
                ;
    }
}

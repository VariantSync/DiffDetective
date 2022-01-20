package datasets.predefined;

import datasets.Repository;
import diff.DiffFilter;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;

public class PaxEngine3 {
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
//            .blockedPaths(DiffEntry.DEV_NULL) // <- I'm with stupid.
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "h")
            .build();

    /**
     * Clones PaxEngine3 from Github.
     * @param localDir Directory to clone the repository to.
     * @return PaxEngine3 repository
     */
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/pmbittner/PaxEngine3", "PaxEngine3")
                .orElseThrow()
                .setDiffFilter(DIFF_FILTER);
    }
}

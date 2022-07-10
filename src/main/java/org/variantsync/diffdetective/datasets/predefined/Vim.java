package org.variantsync.diffdetective.datasets.predefined;

import org.eclipse.jgit.diff.DiffEntry;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.DiffFilter;

import java.nio.file.Path;

/**
 * Default repository for Vim.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class Vim {
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "h", "cpp")
            .build();

    /**
     * Clones Vim from Github.
     * @param localDir Directory to clone the repository to.
     * @return Vim repository
     */
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/vim/vim", "Vim")
                .orElseThrow()
                .setDiffFilter(DIFF_FILTER);
    }
}
package datasets.predefined;

import datasets.Repository;
import diff.DiffFilter;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;

public class SQLite {
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("h", "c")
            .build();

    /**
     * Clones SQLite from Github.
     * @param localDir Directory to clone the repository to.
     * @return SQLite repository
     */
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/sqlite/sqlite.git", "SQLite")
                .orElseThrow()
                .setDiffFilter(DIFF_FILTER);
    }
}

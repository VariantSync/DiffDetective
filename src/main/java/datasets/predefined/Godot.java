package datasets.predefined;

import datasets.Repository;
import diff.DiffFilter;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;

public class Godot {
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("h", "cpp")
            .build();

    /**
     * Clones Godot from Github.
     * @param localDir Directory to clone the repository to.
     * @return Godot repository
     */
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/godotengine/godot.git", "Godot")
                .orElseThrow()
                .setDiffFilter(DIFF_FILTER);
    }
}

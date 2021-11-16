package datasets;

import diff.DiffFilter;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;

/**
 * A collection of default repository datasets.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public final class DefaultRepositories {
    private DefaultRepositories() {}

    private static final Path DIFFDETECTIVE_DEFAULT_REPOSITORIES_DIRECTORY = Path.of("repositories");
    public static final DiffFilter STANCIULESCU_MARLIN_FILTER = new DiffFilter.Builder()
            //.allowBinary(false)
            .allowMerge(false)
            .allowedPaths("Marlin.*")
            .blockedPaths(".*arduino.*")
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "cpp", "h", "pde")
            .build();

    /**
     * Instance for the default predefined Marlin repository.
     * @return Marlin repository
     */
    public static Repository stanciulescuMarlinZip(Path pathToDiffDetective) {
        final Path marlinPath = pathToDiffDetective
                .resolve(DIFFDETECTIVE_DEFAULT_REPOSITORIES_DIRECTORY)
                .resolve("Marlin_old.zip");
        final Repository marlin = Repository.fromZip(marlinPath, "Marlin_old");
        marlin.setDiffFilter(STANCIULESCU_MARLIN_FILTER);
        return marlin;
    }

    /**
     * Clones Linux from Github.
     * @param localPath Path to clone linux to.
     * @return Linux repository
     */
    public static Repository createRemoteLinuxRepo(Path localPath) {
        return Repository
                .tryFromRemote(localPath, "https://github.com/torvalds/linux", "Linux")
                .orElseThrow();
    }

    /**
     * Clones Busybox.
     * @param localPath Path to clone the repository to.
     * @return Busybox repository
     */
    public static Repository createRemoteBusyboxRepo(Path localPath) {
        return Repository
                .tryFromRemote(localPath, "https://git.busybox.net/busybox", "Busybox")
                .orElseThrow();
    }

    /**
     * Clones Vim from Github.
     * @param localPath Path to clone the repository to.
     * @return Vim repository
     */
    public static Repository createRemoteVimRepo(Path localPath) {
        return Repository
                .tryFromRemote(localPath, "https://github.com/vim/vim", "Vim")
                .orElseThrow();
    }

    /**
     * Clones libssh from Gitlab.
     * @param localPath Path to clone the repository to.
     * @return libssh repository
     */
    public static Repository createRemoteLibsshRepo(Path localPath) {
        return Repository
                .tryFromRemote(localPath, "https://gitlab.com/libssh/libssh-mirror", "libssh")
                .orElseThrow();
    }
}

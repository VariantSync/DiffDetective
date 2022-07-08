package org.variantsync.diffdetective.datasets;

import org.eclipse.jgit.api.Git;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.DiffFilter;
import org.variantsync.diffdetective.load.GitLoader;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.functjonal.Lazy;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Representation of git repositories used as datasets for DiffDetective.
 * 
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class Repository {
    public static final Path DIFFDETECTIVE_DEFAULT_REPOSITORIES_DIRECTORY = Path.of("repositories");

    /**
	 * The location from where the input repository is read from.
	 */
	private final RepositoryLocationType repoLocation;
	
	/**
	 * The local path where the repository can be found or should be cloned to.
	 */
	private final Path localPath;

	/**
	 * The remote url of the repository. May be <code>null</code> if local.
	 */
	private final URI remote;

	/**
	 * The name of the repository. Used for debugging.
	 */
	private final String repositoryName;

	/**
	 * Filter determining which files and commits to consider for diffs.
	 */
	private DiffFilter diffFilter;

    /**
     * Options to configure parsing and memory consumption (e.g., by not keeping full diffs in memory).
     */
	private ParseOptions parseOptions;

	private final Lazy<Git> git = Lazy.of(this::load);
	
	/**
	 * Creates a repository.
	 * 
	 * @param repoLocation {@link RepositoryLocationType} From which location the repository is read from
	 * @param localPath The local path where the repository can be found or should be cloned to.
	 * @param remote The remote url of the repository. May be <code>null</code> if local.
	 * @param repositoryName Name of the cloned repository (<code>null</code> if local)
	 * @param parseOptions Omit some debug data to save RAM.
	 * @param diffFilter Filter determining which files and commits to consider for diffs.
	 */
	public Repository(
			final RepositoryLocationType repoLocation,
			final Path localPath,
			final URI remote,
			final String repositoryName,
			final ParseOptions parseOptions,
			final DiffFilter diffFilter) {
		this.repoLocation = repoLocation;
		this.localPath = localPath;
		this.remote = remote;
		this.repositoryName = repositoryName;
		this.parseOptions = parseOptions;
		this.diffFilter = diffFilter;
	}

	/**
	 * Creates repository of the given source and with all other settings set to default values.
	 * @see Repository
	 */
	public Repository(
			final RepositoryLocationType repoLocation,
			final Path localPath,
			final URI remote,
			final String repositoryName) {
		this(repoLocation, localPath, remote, repositoryName,
                ParseOptions.Default, DiffFilter.ALLOW_ALL);
	}

	/**
	 * Creates a repository from an existing directory.
	 * 
	 * @param dirPath The path to the repo directory relative to {@code <WORKING_DIRECTORY>/repositories}
	 * @param repoName A name for the repository (currently not used)
	 * @return A repository from an existing directory
	 */
	public static Repository fromDirectory(Path dirPath, String repoName) {
		return new Repository(
				RepositoryLocationType.FROM_DIR,
				dirPath,
				null,
				repoName);
	}
	
	/**
	 * Creates a repository from a local zip file.
	 * 
	 * @param filePath The path to the zip file (absolute or relative to {@code <WORKING_DIRECTORY>}).
	 * @param repoName A name for the repository (currently not used)
	 * @return A repository from a local zip file
	 */
	public static Repository fromZip(Path filePath, String repoName) {
		return new Repository(
				RepositoryLocationType.FROM_ZIP,
				filePath,
				null,
				repoName);
	}

	/**
	 * Creates a repository from a remote repository.
	 *
	 * @param localPath Path to clone the repository to.
	 * @param repoUri The address of the remote repository
	 * @param repoName Name of the folder, where the git repository is cloned to
	 * @return A repository from a remote location (e.g. Github repository)
	 */
	public static Repository fromRemote(Path localPath, URI repoUri, String repoName) {
		return new Repository(
				RepositoryLocationType.FROM_REMOTE,
				localPath,
				repoUri,
				repoName);
	}

	/**
	 * Creates a repository from a remote repository.
	 *
	 * @param localDir Directory to clone the repository to.
	 * @param repoUri The address of the remote repository
	 * @param repoName Name of the folder, where the git repository is cloned to
	 * @return A repository from a remote location (e.g. Github repository)
	 */
	public static Optional<Repository> tryFromRemote(Path localDir, String repoUri, String repoName) {
		return IO
				.tryParseURI(repoUri)
				.map(remote -> fromRemote(localDir.resolve(repoName), remote, repoName));
	}

	/**
	 * @return the location type indicating how this repository is stored.
	 */
	public RepositoryLocationType getRepoLocation() {
		return repoLocation;
	}

	/**
	 * The path to the repository on disk.
	 * The path points to the root directory of the repository if the repository is stored in a directory.
	 * The path points to a zip file if the repository is stored in a zip file.
	 * The path points to a (possibly not existing) directory to which the repository should be cloned to if the
	 * repository is stored on a remote server.
	 * @see Repository#getRepoLocation()
	 * @see RepositoryLocationType
	 * @return The path to the repository on disk.
	 */
	public Path getLocalPath() {
		return localPath;
	}

	/**
	 * URI of the origin of this repository (i.e., usually the location on a server where this repository was cloned from).
	 */
	public URI getRemoteURI() {
		return remote;
	}

	/**
	 * The name of this repository. Should be unique.
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * Set options for parsing parts of this repository's evolution history.
	 * @param parseOptions Options for parsing the evolution history.
	 * @return this
	 */
	public Repository setParseOptions(ParseOptions parseOptions) {
		this.parseOptions = parseOptions;
        return this;
	}

	/**
	 * Set the diff filter for reading this repository.
	 * The diff filter decides which commits and files should be considered for analyses.
	 * @param filter Filter to apply when traversing this repository's commit history.
	 * @return this
	 */
	public Repository setDiffFilter(final DiffFilter filter) {
		this.diffFilter = filter;
        return this;
	}

	/**
	 * The diff filter decides which commits and files should be considered for analyses.
	 */
	public DiffFilter getDiffFilter() {
		return diffFilter;
	}


	/**
	 * Options that should be used when parsing the evolution history.
	 */
	public ParseOptions getParseOptions() {
		return parseOptions;
	}

	/**
	 * Returns the internal jgit representation of this repository that allows to inspect the repositories history and content.
	 */
	public Lazy<Git> getGitRepo() {
		return git;
	}

	/**
	 * Loads this repository and returns a jgit representation to access it.
	 */
	private Git load() {
		Logger.info("Loading git at {} ...", getLocalPath());
		return switch (getRepoLocation()) {
			case FROM_DIR -> GitLoader.fromDirectory(getLocalPath());
			case FROM_ZIP -> GitLoader.fromZip(getLocalPath());
			case FROM_REMOTE -> GitLoader.fromRemote(getLocalPath(), getRemoteURI());
			default -> throw new UnsupportedOperationException("Unknown git repo source");
		};
	}
}
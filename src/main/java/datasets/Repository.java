package datasets;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Repository containing data.
 * 
 * @author Kevin Jedelhauser
 */
public class Repository {
	
	/**
	 * From where the input is read from.
	 */
	private LoadingParameter load;
	
	/**
	 * The path location for the computation output. 
	 */
	private final Path outputPath;
	
	/**
	 * The local or remote path of a repository.
	 */
	private final String repositoryPath;

	/**
	 * The name of a remote repository. May be <code>null</code> if local. 
	 */
	private final String repositoryName;
	
	/**
	 * For large repositories.
	 * Alternatively, increasing the java heap size also helps.
	 */
	private final boolean saveMemory;
	
	/**
	 * Creates a repository.
	 * 
	 * @param outputPath
	 * @param load {@link LoadingParameter}
	 * @param repositoryPath
	 * @param repositoryName
	 * @param saveMemory
	 */
	private Repository(final Path outputPath,
			final LoadingParameter load,
			final String repositoryPath,
			final String repositoryName,
			final boolean saveMemory) {
		this.outputPath = outputPath;
		this.load = load;
		this.repositoryPath = repositoryPath;
		this.repositoryName = repositoryName;
		this.saveMemory = saveMemory;
	}

	/**
	 * Creates a repository from an existing directory.
	 * 
	 * @param dirPath The directory path.
	 * @return
	 */
	public static Repository createLocalDirRepo(String dirPath) {
		return new Repository(Paths.get("linegraph", "data", "difftrees.lg"),
				LoadingParameter.FROM_DIR,
				dirPath,
				null,
				true);
	}
	
	/**
	 * Creates a repository from a local zip file.
	 * 
	 * @param filePath The file path.
	 * @return
	 */
	public static Repository createLocalZipRepo(String filePath) {
		return new Repository(Paths.get("linegraph", "data", "difftrees.lg"),
				LoadingParameter.FROM_ZIP,
				filePath,
				null,
				true);
	}

	/**
	 * Creates a repository from a remote repository.
	 * 
	 * @param repoUri The address to the remote repository.
	 * @param repoName Name of the folder, where the git repository is cloned to.
	 * @return
	 */
	public static Repository createRemoteRepo(String repoUri, String repoName) {
		return new Repository(Paths.get("linegraph", "data", "difftrees.lg"),
				LoadingParameter.FROM_REMOTE,
				repoUri,
				repoName,
				true);
	}
	
	/**
	 * Create a predefined Marlin repository.
	 * @return
	 */		
	public static Repository getMarlinZipRepo() {
		return createLocalZipRepo("Marlin_old.zip");
	}

	/**
	 * Create a predefined Linux repository.
	 * @return
	 */
	public static Repository getLinuxRepo() {
		return createRemoteRepo("https://github.com/torvalds/linux", "linux_remote");
	}

	/**
	 * Create a predefined Busybox repository.
	 * @return
	 */
	public static Repository getBusyboxRepo() {
		return createRemoteRepo("https://git.busybox.net/busybox", "busybox_remote");
	}

	/**
	 * Create a predefined Vim repository.
	 * @return
	 */
	public static Repository getVimRepo() {
		return createRemoteRepo("https://github.com/vim/vim", "vim_remote");
	}

	/**
	 * Create a predefined libssh repository.
	 * @return
	 */
	public static Repository getLibsshRepo() {
		return createRemoteRepo("https://gitlab.com/libssh/libssh-mirror", "libssh_remote");
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public LoadingParameter getLoad() {
		return load;
	}
	
	public String getRepositoryPath() {
		return repositoryPath;
	}
	
	public String getRepositoryName() {
		return repositoryName;
	}

	public boolean isSaveMemory() {
		return saveMemory;
	}
}
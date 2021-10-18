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
	 * The path location for the computation output. 
	 */
	private final Path outputPath;
	
	/**
	 * <code>True</code> if the input is read from a directory.
	 */
	private final boolean loadFromDir;
	
	/**
	 * <code>True</code> if the input is read from a zip file.
	 */
	private final boolean loadFromZip;
	
	/**
	 * <code>True</code> if the input is read from a remote repository.
	 */
	private final boolean loadFromRemote;
	
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
	 * <code>True</code> if the output data is rendered in a graph.
	 */
	private final boolean renderOutput;
	
	/**
	 * The level of the tree to expand most at to.
	 */
	private final int treesToExportAtMost;
	
	/**
	 * Creates a repository.
	 * 
	 * @param outputPath
	 * @param loadFromDir
	 * @param loadFromZip
	 * @param loadFromRemote
	 * @param repositoryPath
	 * @param repositoryName
	 * @param saveMemory
	 * @param renderOutput
	 * @param treesToExportAtMost
	 */
	private Repository(final Path outputPath,
			final boolean loadFromDir,
			final boolean loadFromZip,
			final boolean loadFromRemote,
			final String repositoryPath,
			final String repositoryName,
			final boolean saveMemory,
			final boolean renderOutput,
			final int treesToExportAtMost) {
		this.outputPath = outputPath;
		this.loadFromDir = loadFromDir;
		this.loadFromZip = loadFromZip;
		this.loadFromRemote = loadFromRemote;
		this.repositoryPath = repositoryPath;
		this.repositoryName = repositoryName;
		this.saveMemory = saveMemory;
		this.renderOutput = renderOutput;
		this.treesToExportAtMost = treesToExportAtMost;
		
		if (!(loadFromDir || loadFromZip || loadFromRemote) || 
				loadFromDir && loadFromZip || 
				loadFromZip && loadFromRemote || 
				loadFromRemote && loadFromDir) {
			throw new IllegalArgumentException("Exactly one load parameter may be true.");
		}
	}

	/**
	 * Creates a repository from an existing directory.
	 * 
	 * @param dirPath
	 * @return
	 */
	public static Repository createLocalDirRepo(String dirPath) {
		return new Repository(Paths.get("linegraph", "data", "difftrees.lg"),
				true,
				false,
				false,
				dirPath,
				null,
				true,
				false,
				-1);
	}
	
	/**
	 * Creates a repository from a local zip file.
	 * 
	 * @param filePath
	 * @return
	 */
	public static Repository createLocalZipRepo(String filePath) {
		return new Repository(Paths.get("linegraph", "data", "difftrees.lg"),
				false,
				true,
				false,
				filePath,
				null,
				true,
				false,
				-1);
	}

	/**
	 * Creates a repository from a remote repository.
	 * 
	 * @param repoUri
	 * @param repoName
	 * @return
	 */
	public static Repository createRemoteRepo(String repoUri, String repoName) {
		return new Repository(Paths.get("linegraph", "data", "difftrees.lg"),
				false,
				false,
				true,
				repoUri,
				repoName,
				true,
				false,
				-1);
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
		return createRemoteRepo("https://github.com/torvalds/linux", "");
	}

	/**
	 * Create a predefined Busybox repository.
	 * @return
	 */
	public static Repository getBusyboxRepo() {
		return createRemoteRepo("https://git.busybox.net/busybox", "");
	}

	/**
	 * Create a predefined Vim repository.
	 * @return
	 */
	public static Repository getVimRepo() {
		return createRemoteRepo("https://github.com/vim/vim", "");
	}

	/**
	 * Create a predefined libssh repository.
	 * @return
	 */
	public static Repository getLibsshRepo() {
		return createRemoteRepo("https://gitlab.com/libssh/libssh-mirror", "");
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public boolean isLoadFromDir() {
		return loadFromDir;
	}
	
	public boolean isLoadFromZip() {
		return loadFromZip;
	}
	
	public boolean isLoadFromRemote() {
		return loadFromRemote;
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

	public boolean isRenderOutput() {
		return renderOutput;
	}

	public int getTreesToExportAtMost() {
		return treesToExportAtMost;
	}
}
package datasets;

/**
 * Read the input from a local directory, a local zip file or a remote repository.
 * 
 * @author Kevin Jedelhauser
 */
public enum LoadingParameter {
	
	/**
	 * Load from a local directory.
	 */
	FROM_DIR,
	
	/**
	 * Load from a local zip file.
	 */
	FROM_ZIP,
	
	/**
	 * Load from a remote repository.
	 */
	FROM_REMOTE
}

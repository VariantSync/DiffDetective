package diff.difftree;

/**
 * Constants that are related to line graph.
 */
public class LineGraphConstants {
	
	/**
	 * Declaration of a {@link DiffTree} in a line graph.
	 */
	public static final String LG_TREE_HEADER = "t #";
	
	/**
	 * Delimiter used in {@link DiffTree DiffTrees}.
	 */
	public static final String TREE_NAME_SEPARATOR = "$$$";
	
	/**
	 * Delimiter used in {@link DiffTree DiffTrees} for regular expressions.
	 * {@link #TREE_NAME_SEPARATOR}
	 */
	public static final String TREE_NAME_SEPARATOR_REGEX = "\\$\\$\\$";
	
	/**
	 * Declaration of a {@link DiffNode} in a line graph.
	 */
	public static final String LG_NODE = "v";
	
	/**
	 * Declaration of a connection between two {@link DiffNode DiffNodes} in a line graph.
	 */
	public static final String LG_EDGE = "e";
	
	/**
	 * An edge between two {@link DiffNode DiffNodes} that has not been altered.
	 */
	public final static String BEFORE_AND_AFTER_PARENT = "ba";

	/**
	 * An edge between two {@link DiffNode DiffNodes} that exists after the edit only.
	 */
	public final static String AFTER_PARENT = "a";

	/**
	 * An edge between two {@link DiffNode DiffNodes} that existed before the edit only.
	 */
	public final static String BEFORE_PARENT = "b";

}

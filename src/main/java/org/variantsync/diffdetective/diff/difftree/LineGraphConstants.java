package org.variantsync.diffdetective.diff.difftree;

/**
 * Constants that are related to line graph IO.
 */
public class LineGraphConstants {
	
	/**
	 * Declaration of a {@link DiffTree} in a line graph.
	 */
	public static final String LG_TREE_HEADER = "t #";

	/**
	 * Declaration of a {@link org.variantsync.diffdetective.relationshipedges.EdgeTypedDiff} in a line graph.
	 */
	public static final String LG_ETTREE_HEADER = "ett #";
	
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
	 * Declaration of an implication edge between two {@link DiffNode DiffNodes} in a line graph.
	 */
	public static final String LG_IMPLEDGE = "ie";

	/**
	 * Declaration of an alternative edge between two {@link DiffNode DiffNodes} in a line graph.
	 */
	public static final String LG_ALTEDGE = "ae";
	
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

	/**
	 * An relationship edge between two {@link DiffNode DiffNodes} which existence is time independent.
	 */
	public final static String TIME_INDEPENDENT = "x";
}

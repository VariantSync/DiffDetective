package org.variantsync.diffdetective.analysis;

/**
 * Collection of constants for keys used in metadata files
 * @author Paul Bittner
 */
public final class MetadataKeys {
    public static final String TASKNAME = "analysis";
    public static final String REPONAME = "repository";

    public final static String TREEFORMAT = "treeformat";
    public final static String NODEFORMAT = "nodeformat";
    public final static String EDGEFORMAT = "edgeformat";

    public final static String NON_NODE_COUNT = "#NON nodes";
    public final static String ADD_NODE_COUNT = "#ADD nodes";
    public final static String REM_NODE_COUNT = "#REM nodes";

    public final static String TOTAL_COMMITS = "total commits";
    public final static String FILTERED_COMMITS = "filtered commits";
    public final static String FAILED_COMMITS = "failed commits";
    public final static String EMPTY_COMMITS = "empty commits";
    public final static String PROCESSED_COMMITS = "processed commits";

    public final static String RUNTIME = "runtime in seconds";
    public final static String RUNTIME_WITH_MULTITHREADING = "runtime with multithreading in seconds";
    public final static String EDGE_ADDING_TIME = "relationship edge adding in ms";
    public static final String MINCOMMIT = "fastestCommit";
    public static final String MAXCOMMIT = "slowestCommit";
    public final static String TREES = "tree diffs";
    public final static String IMPLICATION_EDGES = "implication edges";
    public final static String ALTERNATIVE_EDGES = "alternative edges";
    public final static String FALSE_NODES= "nodes with feature formula FALSE";
}

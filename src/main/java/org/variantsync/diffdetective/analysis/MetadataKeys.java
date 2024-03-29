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
    public static final String MINCOMMIT = "fastestCommit";
    public static final String MAXCOMMIT = "slowestCommit";

    public final static String TOTAL_PATCHES = "total patches";
    public final static String PROCESSED_PATCHES = "processed patches";

    public final static String EXPORTED_COMMITS = "exported commits";
    public final static String EXPORTED_TREES = "exported trees";

    public final static String EDIT_CLASS_MOVEMENT = "edit class movement";
}

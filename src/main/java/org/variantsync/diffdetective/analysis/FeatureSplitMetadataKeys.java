package org.variantsync.diffdetective.analysis;

public final class FeatureSplitMetadataKeys {

    public static final String FEATURE_EXTRACTION_TIME = "Feature extraction time";

    public static final String TOTAL_PATCHES = "total patches";
    public static final String TOTAL_FEATURES = "total features";
    public static final String TOTAL_FEATURE_AWARE_PATCHES = "total feature-aware patches from a feature";
    public static final String TOTAL_REMAINDER_PATCHES = "total remainder patches from a feature";
    public static final String RATIO_OF_FA_PATCHES = "ratio of feature-aware patches to the initial patches";

    public static final String INIT_TREE_DIFF_SIZES = "Initial tree diff sizes";
    public static final String FA_TREE_DIFF_SIZES = "feature aware tree diff sizes";
    public static final String RATIO_OF_NODES = "ratio of nodes between initial and feature-split";
    public static final String INVALID_FA_DIFFS = "invalid feature aware diffs";

    public static final String PATCH_TIME_MS = "patch time MS";

    public static final String MAX_DIFF_SIZE = "Diff size of biggest variation diff";

    public static final String PATCH_STATS = "patch stats";

    public static final String RESULTING_NUM_OF_PATCHES = "number of extracted patches";
    public static final String AVERAGE_NUM_FA_PATCHES = "Average number of feature aware patches over all features";


    public static final String FEATURE = "current feature";
    public static final String NUM_OF_FEATURES = "num of features in a feature aware diff";
    public static final String FEATURE_AWARE_DIFF_SIZE = "feature-aware diff size";

}

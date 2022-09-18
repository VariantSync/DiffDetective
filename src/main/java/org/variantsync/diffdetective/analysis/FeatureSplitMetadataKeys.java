package org.variantsync.diffdetective.analysis;

public final class FeatureSplitMetadataKeys {
    public static final String PATCH_STATS = "patch stats";

    public static final String TOTAL_PATCHES = "total patches";
    public static final String TOTAL_FEATURES = "total features";
    public static final String TOTAL_FEATURE_AWARE_PATCHES = "total feature-aware patches (FA + remaining)";
    public static final String AVERAGE_NUM_FA_PATCHES = "Average number of feature aware patches over all features";
    public static final String RATIO_OF_FA_PATCHES = "ratio of feature-aware patches to the initial patches";

    public static final String INVALID_FA_DIFFS = "invalid feature aware diffs";

    public static final String RATIO_OF_NODES = "ratio of nodes between initial and feature-split";
    public static final String FEATURE = "current feature";
    public static final String NUM_OF_FEATURE_AWARE_PATCHES = "number of extracted feature-aware patches";
    public static final String PATCH_TIME_MS = "patch time MS";
    public static final String NUM_OF_FEATURES = "num of features in a feature aware diff";
    public static final String FEATURE_AWARE_DIFF_SIZE = "feature-aware diff size";

}

package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.category.InplaceMonoid;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.category.Semigroup;
import org.variantsync.functjonal.map.MergeMap;

import java.util.*;
import java.util.function.BiConsumer;

public class FeatureSplitResult implements Metadata<FeatureSplitResult> {
    public final static String NO_REPO = "<NONE>";
    public final static String EXTENSION = ".metadata.txt"; // exported file extension
    public final static String ERROR_BEGIN = "#Error[";
    public final static String ERROR_END = "]";

    public static Map.Entry<String, BiConsumer<FeatureSplitResult, String>> storeAsCustomInfo(String key) {
        return Map.entry(key, (r, val) -> r.putCustomInfo(key, val));
    }

    /**
     * The stored function adds all results form object b to object a
     */
    public final static InplaceSemigroup<FeatureSplitResult> ISEMIGROUP = (a, b) -> {
        a.totalCommits += b.totalCommits;
        a.exportedCommits += b.exportedCommits;
        a.emptyCommits += b.emptyCommits;
        a.failedCommits += b.failedCommits;
        a.runtimeInSeconds += b.runtimeInSeconds;
        a.runtimeWithMultithreadingInSeconds += b.runtimeWithMultithreadingInSeconds;
        a.featureExtractTime += b.featureExtractTime;
        a.min.set(CommitProcessTime.min(a.min, b.min));
        a.max.set(CommitProcessTime.max(a.max, b.max));
        a.debugData.append(b.debugData);
        a.totalPatches += b.totalPatches;
        a.totalFeatureAwarePatches.putAll(b.totalFeatureAwarePatches);
        a.totalRemainderPatches.putAll(b.totalRemainderPatches);
        a.totalFeatures.addAll(b.totalFeatures);
        a.initialTreeDiffSizes.putAll(b.initialTreeDiffSizes);
        a.FAtreeDiffSizes.putAll(b.FAtreeDiffSizes);
        a.ratioOfFAPatches = (a.ratioOfFAPatches + b.ratioOfFAPatches) / 2;
        a.ratioNodes = (a.ratioNodes + b.ratioNodes) / 2;
        MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
        a.diffErrors.append(b.diffErrors);
    };

    /**
     * Used in initialization, which requires an empty semigroup
     */
    public static InplaceMonoid<FeatureSplitResult> IMONOID= InplaceMonoid.From(
            FeatureSplitResult::new,
            ISEMIGROUP
    );

    public String repoName;
    public int totalCommits; // total number of commits in the observed history
    public int exportedCommits; // number of processed commits in the observed history. exportedCommits <= totalCommits
    /**
     * Number of commits that were not processed because they had no DiffTrees.
     * A commit is empty iff at least of one of the following conditions is met for every of its patches:
     * - the patch did not edit a C file,
     * - the DiffTree became empty after transformations (this can happen if there are only whitespace changes),
     * - or the patch had syntax errors in its annotations, so the DiffTree could not be parsed.
     */
    public int emptyCommits;
    /**
     * Number of commits that could not be parsed at all because of exceptions when operating JGit.
     *
     * The number of commits that were filtered because they are a merge commit is thus given as
     * totalCommits - exportedCommits - emptyCommits - failedCommits
     */
    public int failedCommits;
    public double runtimeInSeconds;
    public double runtimeWithMultithreadingInSeconds;
    public double featureExtractTime;
    public final CommitProcessTime min, max;
    public final DiffTreeSerializeDebugData debugData;
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    private final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    public int invalidFADiff;

    public int totalPatches;
    public Set<String> totalFeatures;
    public HashMap<String, Integer> totalFeatureAwarePatches;
    public HashMap<String, Integer> totalRemainderPatches;
    public HashMap<String, List<Integer>> FAtreeDiffSizes;
    public HashMap<String, Integer> initialTreeDiffSizes;
    public double ratioNodes;
    public double ratioOfFAPatches;

    public FeatureSplitResult() {
        this(NO_REPO);
    }

    public FeatureSplitResult(final String repoName) {
        this(
                repoName,
                0, 0, 0, 0, 0,
                0,
                0, 
                CommitProcessTime.Unknown(repoName, Long.MAX_VALUE),
                CommitProcessTime.Unknown(repoName, Long.MIN_VALUE),
                0,0, new HashMap<>(), new HashMap<>(), new HashSet<>(),
                new HashMap<>(), new HashMap<>(), 0.0, 1.0, new DiffTreeSerializeDebugData());
    }

    public FeatureSplitResult(
            final String repoName,
            int totalCommits,
            int exportedCommits,
            int emptyCommits,
            int failedCommits,
            double runtimeInSeconds,
            double runtimeWithMultithreadingInSeconds,
            double featureExtractTime,
            final CommitProcessTime min,
            final CommitProcessTime max,
            int invalidFADiff,
            int totalPatches,
            HashMap<String, Integer> totalFeatureAwarePatches,
            HashMap<String, Integer> totalRemainderPatches,
            Set<String> totalFeatures,
            HashMap<String, Integer> initialTreeDiffSizes,
            HashMap<String, List<Integer>> FAtreeDiffSizes,
            double ratioNodes,
            double ratioOfFAPatches,
            final DiffTreeSerializeDebugData debugData)
    {
        this.repoName = repoName;
        this.totalCommits = totalCommits;
        this.exportedCommits = exportedCommits;
        this.emptyCommits = emptyCommits;
        this.failedCommits = failedCommits;
        this.runtimeInSeconds = runtimeInSeconds;
        this.runtimeWithMultithreadingInSeconds = runtimeWithMultithreadingInSeconds;
        this.featureExtractTime = featureExtractTime;
        this.debugData = debugData;
        this.min = min;
        this.max = max;
        this.totalPatches = totalPatches;
        this.totalFeatureAwarePatches = totalFeatureAwarePatches;
        this.totalRemainderPatches = totalRemainderPatches;
        this.totalFeatures = totalFeatures;
        this.FAtreeDiffSizes = FAtreeDiffSizes;
        this.initialTreeDiffSizes = initialTreeDiffSizes;
        this.ratioNodes = ratioNodes;
        this.ratioOfFAPatches = ratioOfFAPatches;
    }

    /**
     * Stores custom data
     */
    public void putCustomInfo(final String key, final String value) {
        customInfo.put(key, value);
    }

    public void reportDiffErrors(final List<DiffError> errors) {
        for (final DiffError e : errors) {
            diffErrors.put(e, 1);
        }
    }

    /**
     * Creates a key-value store of metadata generated FeatureSplit
     * @return A LinkedHashMap that stores all relevant properties to export.
     */
    @Override
    public LinkedHashMap<String, Object> snapshot() {
        LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
        snap.put(MetadataKeys.REPONAME, repoName);
        snap.put(MetadataKeys.TOTAL_COMMITS, totalCommits);
        snap.put(MetadataKeys.FAILED_COMMITS, failedCommits);
        snap.put(MetadataKeys.EMPTY_COMMITS, emptyCommits);
        snap.put(MetadataKeys.PROCESSED_COMMITS, exportedCommits);
        snap.put(FeatureSplitMetadataKeys.TOTAL_PATCHES, totalPatches);
        snap.put(FeatureSplitMetadataKeys.TOTAL_FEATURES, totalFeatures.size());
        // RQ1.1
        snap.put(FeatureSplitMetadataKeys.RATIO_OF_FA_PATCHES, ratioOfFAPatches);

        // RQ1.2
        snap.put(FeatureSplitMetadataKeys.INIT_TREE_DIFF_SIZES, initialTreeDiffSizes);
        snap.put(FeatureSplitMetadataKeys.FA_TREE_DIFF_SIZES, FAtreeDiffSizes.toString());
        // todo treeDiff size stored in buckets of each commit

        // RQ1.3
        snap.put(FeatureSplitMetadataKeys.INVALID_FA_DIFFS, invalidFADiff);

        // RQ2.1
        snap.put(MetadataKeys.MINCOMMIT, min.toString());
        snap.put(MetadataKeys.MAXCOMMIT, max.toString());
        snap.put(MetadataKeys.RUNTIME, runtimeInSeconds);
        snap.put(MetadataKeys.RUNTIME_WITH_MULTITHREADING, runtimeWithMultithreadingInSeconds);

        // RQ2.2
        // TODO process time of each commit

        snap.put(FeatureSplitMetadataKeys.TOTAL_FEATURE_AWARE_PATCHES, totalFeatureAwarePatches);
        snap.put(FeatureSplitMetadataKeys.TOTAL_REMAINDER_PATCHES, totalRemainderPatches);

        snap.put(FeatureSplitMetadataKeys.RATIO_OF_NODES, ratioNodes);
        snap.put(FeatureSplitMetadataKeys.FEATURE_EXTRACTION_TIME, featureExtractTime);
        snap.putAll(customInfo);
        snap.putAll(debugData.snapshot());
        snap.putAll(Functjonal.bimap(diffErrors, error -> ERROR_BEGIN + error + ERROR_END, Object::toString));
        return snap;
    }

    @Override
    public InplaceSemigroup<FeatureSplitResult> semigroup() {
        return ISEMIGROUP;
    }
}

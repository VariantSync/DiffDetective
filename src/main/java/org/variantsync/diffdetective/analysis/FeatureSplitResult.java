package org.variantsync.diffdetective.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.functjonal.category.InplaceMonoid;
import org.variantsync.functjonal.category.InplaceSemigroup;

public class FeatureSplitResult extends AnalysisResult<FeatureSplitResult> {
    /**
     * The stored function adds all results form object b to object a
     */
    public final static InplaceSemigroup<FeatureSplitResult> ISEMIGROUP = (a, b) -> {
        a.featureExtractTime += b.featureExtractTime;
        a.totalFeatureAwarePatches.putAll(b.totalFeatureAwarePatches);
        a.totalRemainderPatches.putAll(b.totalRemainderPatches);
        a.totalFeatures.addAll(b.totalFeatures);
        a.initialTreeDiffSizes.putAll(b.initialTreeDiffSizes);
        a.FAtreeDiffSizes.putAll(b.FAtreeDiffSizes);
        a.patchTimes.putAll(b.patchTimes);
        a.ratioOfFAPatches = (a.ratioOfFAPatches + b.ratioOfFAPatches) / 2;
        a.ratioNodes = (a.ratioNodes + b.ratioNodes) / 2;
    };

    /**
     * Used in initialization, which requires an empty semigroup
     */
    public static InplaceMonoid<FeatureSplitResult> IMONOID= InplaceMonoid.From(
            FeatureSplitResult::new,
            ISEMIGROUP
    );

    public double featureExtractTime = 0;
    public int invalidFADiff = 0;
    public Set<String> totalFeatures = new HashSet<>();
    public HashMap<String, Integer> totalFeatureAwarePatches = new HashMap<>();
    public HashMap<String, Integer> totalRemainderPatches = new HashMap<>();
    public HashMap<String, Integer> initialTreeDiffSizes = new HashMap<>();
    public HashMap<String, List<Integer>> FAtreeDiffSizes = new HashMap<>();
    public HashMap<String, List<Long>> patchTimes = new HashMap<>();
    public double ratioNodes = 0.0;
    public double ratioOfFAPatches = 1.0;

    public FeatureSplitResult() {
    }

    public FeatureSplitResult(Set<String> totalFeatures) {
        this.totalFeatures = totalFeatures;
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
        LinkedHashMap<String, Object> snap = super.snapshot();
        snap.put(FeatureSplitMetadataKeys.TOTAL_FEATURES, totalFeatures);
        // RQ1.1
        snap.put(FeatureSplitMetadataKeys.RATIO_OF_FA_PATCHES, ratioOfFAPatches);

        // RQ1.2
        snap.put(FeatureSplitMetadataKeys.INIT_TREE_DIFF_SIZES, initialTreeDiffSizes);
        snap.put(FeatureSplitMetadataKeys.FA_TREE_DIFF_SIZES, FAtreeDiffSizes.toString());

        // RQ1.3
        snap.put(FeatureSplitMetadataKeys.INVALID_FA_DIFFS, invalidFADiff);

        // RQ2.1
        // Handled by `AnalysisResult`
        // MetadataKeys.MINCOMMIT in min
        // MetadataKeys.MAXCOMMIT in max
        // MetadataKeys.RUNTIME in runtimeInSeconds
        // MetadataKeys.RUNTIME_WITH_MULTITHREADING in runtimeWithMultithreadingInSeconds

        // RQ2.2
        snap.put(FeatureSplitMetadataKeys.PATCH_TIME_MS, patchTimes.toString());

        return snap;
    }

    @Override
    public InplaceSemigroup<FeatureSplitResult> semigroup() {
        return ISEMIGROUP;
    }
}

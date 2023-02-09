package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.util.Clock;

/**
 * Generate a list of all features contained in a repo
 */
public class FeatureSplitFeatureExtraction implements Analysis.Hooks<FeatureSplitResult> {
    private final Clock featureExtractTime = new Clock();

    @Override
    public void beginBatch(Analysis<FeatureSplitResult> analysis) {
        featureExtractTime.start();
    }

    @Override
    public boolean analyzeDiffTree(Analysis<FeatureSplitResult> analysis) throws Exception {
        // Store all occurring features, which are then used to extract feature aware patches and remaining patches
        analysis.getResult().totalFeatures.addAll(FeatureQueryGenerator.featureQueryGenerator(analysis.getDiffTree()));
        return true;

    }

    @Override
    public void endBatch(Analysis<FeatureSplitResult> analysis) {
        analysis.getResult().featureExtractTime = featureExtractTime.getPassedSeconds();
    }
}

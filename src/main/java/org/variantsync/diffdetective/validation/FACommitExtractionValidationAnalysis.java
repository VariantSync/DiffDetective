package org.variantsync.diffdetective.validation;

import java.util.HashMap;
import java.util.Set;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FeatureSplitResult;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.transform.FeatureSplit;

public class FACommitExtractionValidationAnalysis implements Analysis.Hooks<FeatureSplitResult> {
    private Set<String> randomFeatures;

    public FACommitExtractionValidationAnalysis(Set<String> randomFeatures) {
        this.randomFeatures = randomFeatures;
    }

    @Override
    public boolean analyzeDiffTree(Analysis<FeatureSplitResult> analysis) {
        // Add features to results
        randomFeatures.forEach(feature -> {
            if (analysis.getResult().totalFeatureAwarePatches.get(feature) == null) {
                analysis.getResult().totalFeatureAwarePatches.put(feature, 0);
                analysis.getResult().totalRemainderPatches.put(feature, 0);
            }
        });

        // validate FeatureSplit
        randomFeatures.forEach(feature -> {

            System.out.println(analysis.getDiffTree().toString());
            System.out.println(analysis.getDiffTree().computeSize());

            // generate feature-aware and remaining patches
            HashMap<String, DiffTree> featureAware = FeatureSplit.featureSplit(analysis.getDiffTree(), PropositionalFormulaParser.Default.parse(feature));
            System.out.println("FeatureSplit");

            // 1. get number of feature-aware patches for a patch
            if(featureAware.get(feature) != null) analysis.getResult().totalFeatureAwarePatches.replace(feature, analysis.getResult().totalFeatureAwarePatches.get(feature) + 1);
            if(featureAware.get("remains") != null) analysis.getResult().totalRemainderPatches.replace(feature, analysis.getResult().totalRemainderPatches.get(feature) + 1);

            featureAware.forEach((key, value) -> {
                if (value == null) return;
                // 2. get calculation time for a patch with committimes.txt!!

                // 3. check if patch is valid
                if(!value.isConsistent().isSuccess())  {
                    Logger.error("incorrectly extracted tree");
                    ++analysis.getResult().invalidFADiff;
                }

                // 4. calculate size of feature-aware difftree and size of initial difftree
                int featureDiffSizeRatio = value.computeSize() / analysis.getDiffTree().computeSize();
                analysis.getResult().ratioNodes = (analysis.getResult().ratioNodes + featureDiffSizeRatio) / 2;
            });
        });

        return true;
    }
}

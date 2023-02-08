package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FeatureExtractionAnalysis;
import org.variantsync.diffdetective.analysis.FeatureSplitResult;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.transform.FeatureSplit;

public class FACommitValidation implements Analysis.Hooks<FeatureSplitResult> {
    private Set<String> randomFeatures;

    public static void main(String[] args) throws IOException {
        Validation.run(args, (repo, repoOutputDir) -> {
            Logger.info(" === Begin Feature Extraction {} ===", repo.getRepositoryName());
            var featureExtrationResult =
                Analysis.forEachCommit(
                    () -> new Analysis<>(
                        List.of(
                            new PreprocessingAnalysis<>(new CutNonEditedSubtrees()),
                            new FilterAnalysis<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                            new FeatureExtractionAnalysis()
                        ),
                        repo,
                        repoOutputDir,
                        new FeatureSplitResult()
                    ),
                    1
                );
            Set<String> extractedFeatures = featureExtrationResult.totalFeatures;

            Logger.info(" === Begin Evaluation {} ===", repo.getRepositoryName());

            int numOfFeatures = 3;

            // Select desired features
            Set<String> randomFeatures = new HashSet<>();
            List<String> rndFeatures = new ArrayList<>(extractedFeatures);
            Collections.shuffle(rndFeatures);
            if(rndFeatures.size() >= numOfFeatures) {
                randomFeatures.addAll(rndFeatures.subList(0, numOfFeatures));
            } else {
                randomFeatures.addAll(rndFeatures);
            }

            Analysis.forEachCommit(() -> new Analysis<>(
                List.of(
                    new PreprocessingAnalysis<>(new CutNonEditedSubtrees()),
                    new FilterAnalysis<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    new FACommitValidation(randomFeatures),
                    new StatisticsAnalysis<>()
                ),
                repo,
                repoOutputDir,
                new FeatureSplitResult(randomFeatures)
            ));
        });
    }

    public FACommitValidation(Set<String> randomFeatures) {
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

package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FeatureExtractionAnalysis;
import org.variantsync.diffdetective.analysis.FeatureSplitMetadataKeys;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.transform.FeatureSplit;
import org.variantsync.functjonal.category.InplaceSemigroup;

public class FACommitValidation implements Analysis.Hooks {
    public static final ResultKey<Result> RESULT = new ResultKey<>("FACommitValidation");
    public static final class Result implements Metadata<Result> {
        public int invalidFADiff = 0;
        public HashMap<String, Integer> totalFeatureAwarePatches = new HashMap<>();
        public HashMap<String, Integer> totalRemainderPatches = new HashMap<>();
        public double ratioNodes = 0.0;

        public static final InplaceSemigroup<Result> ISEMIGROUP = (a, b) -> {
            a.totalFeatureAwarePatches.putAll(b.totalFeatureAwarePatches);
            a.totalRemainderPatches.putAll(b.totalRemainderPatches);
            a.ratioNodes = (a.ratioNodes + b.ratioNodes) / 2;
        };

        @Override
        public InplaceSemigroup<Result> semigroup() {
            return ISEMIGROUP;
        }

        /**
         * Creates a key-value store of metadata generated FeatureSplit
         * @return A LinkedHashMap that stores all relevant properties to export.
         */
        @Override
        public LinkedHashMap<String, Object> snapshot() {
            final var snap = new LinkedHashMap<String, Object>();
            // RQ1.3
            snap.put(FeatureSplitMetadataKeys.INVALID_FA_DIFFS, invalidFADiff);

            // RQ2.1
            // Handled by `AnalysisResult`
            // MetadataKeys.MINCOMMIT in min
            // MetadataKeys.MAXCOMMIT in max
            // MetadataKeys.RUNTIME in runtimeInSeconds
            // MetadataKeys.RUNTIME_WITH_MULTITHREADING in runtimeWithMultithreadingInSeconds

            return snap;
        }

        @Override
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            throw new UnsupportedOperationException("TODO Not implemented yet");
        }
    }

    private Set<String> randomFeatures;

    public static void main(String[] args) throws IOException {
        Validation.run(args, (repo, repoOutputDir) -> {
            Logger.info(" === Begin Feature Extraction {} ===", repo.getRepositoryName());
            var featureExtrationResult =
                Analysis.forEachCommit(
                    () -> new Analysis(
                        "FeatureExtraction",
                        List.of(
                            new PreprocessingAnalysis(new CutNonEditedSubtrees()),
                            new FilterAnalysis(DiffTreeFilter.notEmpty()), // filters unwanted trees
                            new FeatureExtractionAnalysis()
                        ),
                        repo,
                        repoOutputDir
                    ),
                    1
                );
            Set<String> extractedFeatures = featureExtrationResult.get(FeatureExtractionAnalysis.RESULT).totalFeatures;

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

            Analysis.forEachCommit(() -> new Analysis(
                "FACommitValidation",
                List.of(
                    new PreprocessingAnalysis(new CutNonEditedSubtrees()),
                    new FilterAnalysis(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    new FACommitValidation(randomFeatures),
                    new StatisticsAnalysis()
                ),
                repo,
                repoOutputDir
            ));
        });
    }

    public FACommitValidation(Set<String> randomFeatures) {
        this.randomFeatures = randomFeatures;
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(RESULT, new Result());
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
        // Add features to results
        randomFeatures.forEach(feature -> {
            if (analysis.get(RESULT).totalFeatureAwarePatches.get(feature) == null) {
                analysis.get(RESULT).totalFeatureAwarePatches.put(feature, 0);
                analysis.get(RESULT).totalRemainderPatches.put(feature, 0);
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
            if(featureAware.get(feature) != null) analysis.get(RESULT).totalFeatureAwarePatches.replace(feature, analysis.get(RESULT).totalFeatureAwarePatches.get(feature) + 1);
            if(featureAware.get("remains") != null) analysis.get(RESULT).totalRemainderPatches.replace(feature, analysis.get(RESULT).totalRemainderPatches.get(feature) + 1);

            featureAware.forEach((key, value) -> {
                if (value == null) return;
                // 2. get calculation time for a patch with committimes.txt!!

                // 3. check if patch is valid
                if(!value.isConsistent().isSuccess())  {
                    Logger.error("incorrectly extracted tree");
                    ++analysis.get(RESULT).invalidFADiff;
                }

                // 4. calculate size of feature-aware difftree and size of initial difftree
                int featureDiffSizeRatio = value.computeSize() / analysis.getDiffTree().computeSize();
                analysis.get(RESULT).ratioNodes = (analysis.get(RESULT).ratioNodes + featureDiffSizeRatio) / 2;
            });
        });

        return true;
    }
}

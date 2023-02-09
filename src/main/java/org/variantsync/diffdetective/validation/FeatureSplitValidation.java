package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FeatureQueryGenerator;
import org.variantsync.diffdetective.analysis.FeatureSplitMetadataKeys;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.transform.FeatureSplit;
import org.variantsync.functjonal.category.InplaceSemigroup;

public class FeatureSplitValidation implements Analysis.Hooks {
    public static final ResultKey<Result> RESULT = new ResultKey<>("FACommitValidation");
    public static final class Result implements Metadata<Result> {
        public final Set<String> totalFeatures = new HashSet<>();
        public int invalidFADiff = 0;
        public final HashMap<String, Integer> initialTreeDiffSizes = new HashMap<>();
        public final HashMap<String, List<Integer>> FAtreeDiffSizes = new HashMap<>();
        public final HashMap<String, List<Long>> patchTimes = new HashMap<>();
        public double ratioNodes = 0.0;
        public double ratioOfFAPatches = 1.0;

        public static final InplaceSemigroup<Result> ISEMIGROUP = (a, b) -> {
            a.totalFeatures.addAll(b.totalFeatures);
            a.initialTreeDiffSizes.putAll(b.initialTreeDiffSizes);
            a.FAtreeDiffSizes.putAll(b.FAtreeDiffSizes);
            a.patchTimes.putAll(b.patchTimes);
            a.ratioOfFAPatches = (a.ratioOfFAPatches + b.ratioOfFAPatches) / 2;
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
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            throw new UnsupportedOperationException("TODO Not implemented yet");
        }
    }

    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
        Validation.run(args, (repo, repoOutputDir) ->
            Analysis.forEachCommit(() -> new Analysis(
                "FeatureSplitValidation",
                List.of(
                    new PreprocessingAnalysis(new CutNonEditedSubtrees()),
                    new FilterAnalysis(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    new FeatureSplitValidation(),
                    new StatisticsAnalysis()
                ),
                repo,
                repoOutputDir
            ))
        );
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(RESULT, new Result());
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
        // calculate sizes of each patch
        analysis.get(RESULT).initialTreeDiffSizes.put(analysis.getPatch().toString(), analysis.getDiffTree().computeSize());

        // Add features to results
        Set<String> allFeatures = FeatureQueryGenerator.featureQueryGenerator(analysis.getDiffTree());
        analysis.get(RESULT).totalFeatures.addAll(allFeatures);

        // validate FeatureSplit
        allFeatures.forEach(feature -> {

            // calculate time of each input patch
            final Clock patchTime = new Clock();
            patchTime.start();

            //System.out.println(t.toString());
            //System.out.println(t.computeSize());

            if(feature == null || PropositionalFormulaParser.Default.parse(feature) == null){
                System.out.println("Parser Error: " + PropositionalFormulaParser.Default.parse(feature));
            }

            // generate feature-aware and remaining patches
            HashMap<String, DiffTree> featureAware = FeatureSplit.featureSplit(analysis.getDiffTree(), PropositionalFormulaParser.Default.parse(feature));

            //System.out.println("FeatureSplit");

            // 1. get number of feature-aware patches for a patch
            analysis.get(RESULT).ratioOfFAPatches = (analysis.get(RESULT).ratioOfFAPatches + featureAware.size()) / 2;

            if(featureAware.get(feature) != null) {
                List<Integer> nodes = analysis.get(RESULT).FAtreeDiffSizes.get(analysis.getPatch().toString());
                if(nodes == null) nodes = new LinkedList<>();
                nodes.add(featureAware.get(feature).computeSize());
                analysis.get(RESULT).FAtreeDiffSizes.put(analysis.getPatch().toString(), nodes);
            }

            featureAware.forEach((key, value) -> {
                if (value == null) return;

                // 3. check if patch is valid
                if(!value.isConsistent().isSuccess())  {
                    Logger.error("incorrectly extracted tree");
                    ++analysis.get(RESULT).invalidFADiff;
                }

                // 4. calculate size of feature-aware difftree and size of initial difftree
                int featureDiffSizeRatio = value.computeSize() / analysis.getDiffTree().computeSize();
                analysis.get(RESULT).ratioNodes = (analysis.get(RESULT).ratioNodes + featureDiffSizeRatio) / 2;
            });

            // calc patch times
            List<Long> nodes = analysis.get(RESULT).patchTimes.get(analysis.getPatch().toString());
            if(nodes == null) nodes = new LinkedList<>();
            nodes.add(patchTime.getPassedMilliseconds());
            analysis.get(RESULT).patchTimes.put(analysis.getPatch().toString(), nodes);
        });

        return true;
    }
}

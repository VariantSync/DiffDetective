package org.variantsync.diffdetective.validation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FeatureQueryGenerator;
import org.variantsync.diffdetective.analysis.FeatureSplitResult;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.transform.FeatureSplit;

public class FeatureSplitValidationAnalysis implements Analysis.Hooks<FeatureSplitResult> {
    @Override
    public boolean analyzeDiffTree(Analysis<FeatureSplitResult> analysis) {
        // calculate sizes of each patch
        analysis.getResult().initialTreeDiffSizes.put(analysis.getPatch().toString(), analysis.getDiffTree().computeSize());

        // Add features to results
        Set<String> allFeatures = FeatureQueryGenerator.featureQueryGenerator(analysis.getDiffTree());
        analysis.getResult().totalFeatures.addAll(allFeatures);

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
            analysis.getResult().ratioOfFAPatches = (analysis.getResult().ratioOfFAPatches + featureAware.size()) / 2;

            if(featureAware.get(feature) != null) {
                List<Integer> nodes = analysis.getResult().FAtreeDiffSizes.get(analysis.getPatch().toString());
                if(nodes == null) nodes = new LinkedList<>();
                nodes.add(featureAware.get(feature).computeSize());
                analysis.getResult().FAtreeDiffSizes.put(analysis.getPatch().toString(), nodes);
            }

            featureAware.forEach((key, value) -> {
                if (value == null) return;

                // 3. check if patch is valid
                if(!value.isConsistent().isSuccess())  {
                    Logger.error("incorrectly extracted tree");
                    ++analysis.getResult().invalidFADiff;
                }

                // 4. calculate size of feature-aware difftree and size of initial difftree
                int featureDiffSizeRatio = value.computeSize() / analysis.getDiffTree().computeSize();
                analysis.getResult().ratioNodes = (analysis.getResult().ratioNodes + featureDiffSizeRatio) / 2;
            });

            // calc patch times
            List<Long> nodes = analysis.getResult().patchTimes.get(analysis.getPatch().toString());
            if(nodes == null) nodes = new LinkedList<>();
            nodes.add(patchTime.getPassedMilliseconds());
            analysis.getResult().patchTimes.put(analysis.getPatch().toString(), nodes);
        });

        return true;
    }
}

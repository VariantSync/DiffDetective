package org.variantsync.diffdetective.analysis;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.functjonal.category.InplaceSemigroup;

/**
 * Generate a list of all features contained in a repo
 */
public class FeatureExtractionAnalysis implements Analysis.Hooks {
    public static final ResultKey<Result> RESULT = new ResultKey<>("FeatureExtractionAnalysis");
    public static final class Result implements Metadata<Result> {
        public final Set<String> totalFeatures = new HashSet<>();
        public double featureExtractTime = 0;

        public static InplaceSemigroup<Result> ISEMIGROUP = (a, b) -> {
            a.totalFeatures.addAll(b.totalFeatures);
            a.featureExtractTime += b.featureExtractTime;
        };

        @Override
        public InplaceSemigroup<Result> semigroup() {
            return ISEMIGROUP;
        }

        @Override
        public LinkedHashMap<String, Object> snapshot() {
            LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
            snap.put(FeatureSplitMetadataKeys.TOTAL_FEATURES, totalFeatures);
            return snap;
        }

        @Override
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            throw new UnsupportedOperationException("TODO Not implemented yet");
        }
    }

    private final Clock featureExtractTime = new Clock();

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(RESULT, new Result());
    }

    @Override
    public void beginBatch(Analysis analysis) {
        featureExtractTime.start();
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        // Store all occurring features, which are then used to extract feature aware patches and remaining patches
        analysis.get(RESULT).totalFeatures.addAll(FeatureQueryGenerator.featureQueryGenerator(analysis.getDiffTree()));
        return true;

    }

    @Override
    public void endBatch(Analysis analysis) {
        analysis.get(RESULT).featureExtractTime = featureExtractTime.getPassedSeconds();
    }
}

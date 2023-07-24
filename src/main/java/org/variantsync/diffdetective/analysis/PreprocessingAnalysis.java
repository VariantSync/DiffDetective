package org.variantsync.diffdetective.analysis;

import java.util.Arrays;
import java.util.List;

import org.variantsync.diffdetective.variation.diff.transform.VariationDiffTransformer;
import org.variantsync.diffdetective.variation.DiffLinesLabel;

public class PreprocessingAnalysis implements Analysis.Hooks {
    private final List<VariationDiffTransformer<DiffLinesLabel>> preprocessors;

    public PreprocessingAnalysis(List<VariationDiffTransformer<DiffLinesLabel>> preprocessors) {
        this.preprocessors = preprocessors;
    }

    @SafeVarargs
    public PreprocessingAnalysis(VariationDiffTransformer<DiffLinesLabel>... preprocessors) {
        this.preprocessors = Arrays.asList(preprocessors);
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) {
        VariationDiffTransformer.apply(preprocessors, analysis.getCurrentVariationDiff());
        analysis.getCurrentVariationDiff().assertConsistency();
        return true;
    }
}

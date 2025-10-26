package org.variantsync.diffdetective.analysis;

import java.util.Arrays;
import java.util.List;

import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.transform.Transformer;
import org.variantsync.diffdetective.variation.DiffLinesLabel;

public class PreprocessingAnalysis implements Analysis.Hooks {
    private final List<Transformer<VariationDiff<DiffLinesLabel>>> preprocessors;

    public PreprocessingAnalysis(List<Transformer<VariationDiff<DiffLinesLabel>>> preprocessors) {
        this.preprocessors = preprocessors;
    }

    @SafeVarargs
    public PreprocessingAnalysis(Transformer<VariationDiff<DiffLinesLabel>>... preprocessors) {
        this.preprocessors = Arrays.asList(preprocessors);
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) {
        Transformer.apply(preprocessors, analysis.getCurrentVariationDiff());
        analysis.getCurrentVariationDiff().assertConsistency();
        return true;
    }
}

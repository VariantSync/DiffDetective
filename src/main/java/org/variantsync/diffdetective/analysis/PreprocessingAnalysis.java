package org.variantsync.diffdetective.analysis;

import java.util.Arrays;
import java.util.List;

import org.variantsync.diffdetective.variation.diff.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.variation.DiffLinesLabel;

public class PreprocessingAnalysis implements Analysis.Hooks {
    private final List<DiffTreeTransformer<DiffLinesLabel>> preprocessors;

    public PreprocessingAnalysis(List<DiffTreeTransformer<DiffLinesLabel>> preprocessors) {
        this.preprocessors = preprocessors;
    }

    @SafeVarargs
    public PreprocessingAnalysis(DiffTreeTransformer<DiffLinesLabel>... preprocessors) {
        this.preprocessors = Arrays.asList(preprocessors);
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
        DiffTreeTransformer.apply(preprocessors, analysis.getCurrentDiffTree());
        analysis.getCurrentDiffTree().assertConsistency();
        return true;
    }
}

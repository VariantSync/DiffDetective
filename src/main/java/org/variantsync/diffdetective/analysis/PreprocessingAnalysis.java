package org.variantsync.diffdetective.analysis;

import java.util.Arrays;
import java.util.List;

import org.variantsync.diffdetective.variation.diff.transform.DiffTreeTransformer;

public class PreprocessingAnalysis implements Analysis.Hooks {
    private List<DiffTreeTransformer> preprocessors;

    public PreprocessingAnalysis(List<DiffTreeTransformer> preprocessors) {
        this.preprocessors = preprocessors;
    }

    public PreprocessingAnalysis(DiffTreeTransformer... preprocessors) {
        this.preprocessors = Arrays.asList(preprocessors);
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        DiffTreeTransformer.apply(preprocessors, analysis.getCurrentDiffTree());
        analysis.getCurrentDiffTree().assertConsistency();
        return true;
    }
}

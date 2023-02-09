package org.variantsync.diffdetective.analysis;

import java.util.Arrays;
import java.util.List;

import org.variantsync.diffdetective.variation.diff.transform.DiffTreeTransformer;

public class PreprocessingAnalysis<T extends AnalysisResult<T>> implements Analysis.Hooks<T> {
    private List<DiffTreeTransformer> preprocessors;

    public PreprocessingAnalysis(List<DiffTreeTransformer> preprocessors) {
        this.preprocessors = preprocessors;
    }

    public PreprocessingAnalysis(DiffTreeTransformer... preprocessors) {
        this.preprocessors = Arrays.asList(preprocessors);
    }

    @Override
    public boolean analyzeDiffTree(Analysis<T> analysis) throws Exception {
        DiffTreeTransformer.apply(preprocessors, analysis.getDiffTree());
        analysis.getDiffTree().assertConsistency();
        return true;
    }
}

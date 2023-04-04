package org.variantsync.diffdetective.experiments.views;

import org.variantsync.diffdetective.analysis.Analysis;

public class ViewAnalysis implements Analysis.Hooks {
    @Override
    public void initializeResults(Analysis analysis) {
        Analysis.Hooks.super.initializeResults(analysis);
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        return Analysis.Hooks.super.analyzeDiffTree(analysis);
    }
}

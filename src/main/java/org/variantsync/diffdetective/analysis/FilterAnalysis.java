package org.variantsync.diffdetective.analysis;

import java.util.Arrays;

import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.filter.TaggedPredicate;

public class FilterAnalysis implements Analysis.Hooks {
    private ExplainedFilter<DiffTree> treeFilter;

    public FilterAnalysis(ExplainedFilter<DiffTree> treeFilter) {
        this.treeFilter = treeFilter;
    }

    @SafeVarargs
    public FilterAnalysis(TaggedPredicate<String, DiffTree>... treeFilter) {
        this.treeFilter = new ExplainedFilter<DiffTree>(Arrays.stream(treeFilter));
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(ExplainedFilterSummary.KEY, new ExplainedFilterSummary());
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        return treeFilter.test(analysis.getDiffTree());
    }

    @Override
    public void endCommit(Analysis analysis) {
        analysis.append(ExplainedFilterSummary.KEY, new ExplainedFilterSummary(treeFilter));
        treeFilter.resetExplanations();
    }
}

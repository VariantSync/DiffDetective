package org.variantsync.diffdetective.analysis;

import java.util.Arrays;

import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.filter.TaggedPredicate;

public class FilterAnalysis implements Analysis.Hooks {
    private ExplainedFilter<VariationDiff<? extends DiffLinesLabel>> treeFilter;

    public FilterAnalysis(ExplainedFilter<VariationDiff<? extends DiffLinesLabel>> treeFilter) {
        this.treeFilter = treeFilter;
    }

    @SafeVarargs
    public FilterAnalysis(TaggedPredicate<String, VariationDiff<? extends DiffLinesLabel>>... treeFilter) {
        this.treeFilter = new ExplainedFilter<>(Arrays.stream(treeFilter));
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(ExplainedFilterSummary.KEY, new ExplainedFilterSummary());
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) throws Exception {
        return treeFilter.test(analysis.getCurrentVariationDiff());
    }

    @Override
    public void endCommit(Analysis analysis) {
        analysis.append(ExplainedFilterSummary.KEY, new ExplainedFilterSummary(treeFilter));
        treeFilter.resetExplanations();
    }
}

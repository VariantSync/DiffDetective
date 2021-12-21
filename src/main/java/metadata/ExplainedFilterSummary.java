package metadata;

import diff.difftree.filter.ExplainedFilter;
import util.Semigroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExplainedFilterSummary implements Semigroup<ExplainedFilterSummary>, Metadata {
    private final List<ExplainedFilter.Explanation> explanations;

    public ExplainedFilterSummary() {
        this.explanations = new ArrayList<>();
    }

    public <T> ExplainedFilterSummary(final ExplainedFilter<T> filter) {
        this.explanations = filter.getExplanations().map(ExplainedFilter.Explanation::new).toList();
    }

    @Override
    public void append(final ExplainedFilterSummary other) {
        this.explanations.addAll(other.explanations);
    }

    @Override
    public Map<String, Integer> snapshot() {
        return explanations.stream().collect(
                Collectors.toMap(
                        e -> "filtered because not (" + e.getName() + ")",
                        ExplainedFilter.Explanation::getFilterCount,
                        Integer::sum,
                        LinkedHashMap::new // LinkedHashMap for insertion-order iteration
                )
        );
    }
}

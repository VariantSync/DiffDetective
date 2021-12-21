package metadata;

import diff.difftree.filter.ExplainedFilter;
import util.Assert;
import util.Semigroup;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExplainedFilterSummary implements Semigroup<ExplainedFilterSummary>, Metadata {
    private final Map<String, ExplainedFilter.Explanation> explanations;

    public ExplainedFilterSummary() {
        this.explanations = new HashMap<>();
    }

    public <T> ExplainedFilterSummary(final ExplainedFilter<T> filter) {
        this.explanations = filter.getExplanations().collect(
                Collectors.toMap(
                        ExplainedFilter.Explanation::getName,
                        ExplainedFilter.Explanation::new
                )
        );
    }

    @Override
    public void append(final ExplainedFilterSummary other) {
        for (final ExplainedFilter.Explanation e : other.explanations.values()) {
            final String key = e.getName();
            if (this.explanations.containsKey(key)) {
                this.explanations.get(key).append(e);
            } else {
                this.explanations.put(key, e);
            }
        };
    }

    @Override
    public Map<String, Integer> snapshot() {
        // LinkedHashMap for insertion-order iteration
        final Map<String, Integer> snap = new LinkedHashMap<>();
        for (final ExplainedFilter.Explanation e : explanations.values()) {
            final String explName = "filtered because not (" + e.getName() + ")";
            Assert.assertTrue(!snap.containsKey(explName));
            snap.put(explName, e.getFilterCount());
        }
        return snap;
    }
}

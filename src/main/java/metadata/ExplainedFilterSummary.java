package metadata;

import diff.difftree.filter.ExplainedFilter;
import util.Functional;
import util.Semigroup;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class ExplainedFilterSummary implements Metadata<ExplainedFilterSummary> {
    private final LinkedHashMap<String, ExplainedFilter.Explanation> explanations;

    public ExplainedFilterSummary() {
        this.explanations = new LinkedHashMap<>();
    }

    public <T> ExplainedFilterSummary(final ExplainedFilter<T> filter) {
        this.explanations = filter.getExplanations().collect(
                Collectors.toMap(
                        ExplainedFilter.Explanation::getName,
                        ExplainedFilter.Explanation::new,
                        (e1, e2) -> {throw new UnsupportedOperationException("Unexpected merging of two explanations \"" + e1 + "\" and \"" + e2 + "\".");},
                        LinkedHashMap::new
                )
        );
    }

    @Override
    public void append(final ExplainedFilterSummary other) {
        for (final ExplainedFilter.Explanation e : other.explanations.values()) {
            Semigroup.appendValue(this.explanations, e.getName(), e);
        }
    }

    @Override
    public LinkedHashMap<String, Integer> snapshot() {
        return Functional.bimap(
                explanations,
                name -> "filtered because not (" + name + ")",
                ExplainedFilter.Explanation::getFilterCount,
                LinkedHashMap::new
        );
    }
}

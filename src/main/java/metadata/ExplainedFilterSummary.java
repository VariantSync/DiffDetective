package metadata;

import de.variantsync.functjonal.Functjonal;
import diff.difftree.filter.ExplainedFilter;
import util.semigroup.InlineSemigroup;
import util.semigroup.MergeMap;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class ExplainedFilterSummary implements Metadata<ExplainedFilterSummary> {
    public static final InlineSemigroup<ExplainedFilterSummary> ISEMIGROUP =
            (a, b) -> MergeMap.putAllValues(a.explanations, b.explanations, ExplainedFilter.Explanation.ISEMIGROUP);

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
    public LinkedHashMap<String, Integer> snapshot() {
        return Functjonal.bimap(
                explanations,
                name -> "filtered because not (" + name + ")",
                ExplainedFilter.Explanation::getFilterCount,
                LinkedHashMap::new
        );
    }

    @Override
    public InlineSemigroup<ExplainedFilterSummary> semigroup() {
        return ISEMIGROUP;
    }
}

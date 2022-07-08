package org.variantsync.diffdetective.metadata;

import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.map.MergeMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metadata that stores the reasons why an {@link ExplainedFilter} filtered data.
 * @author Paul Bittner
 */
public class ExplainedFilterSummary implements Metadata<ExplainedFilterSummary> {
    /**
     * Prefix for exported filter reasons.
     */
    public static final String FILTERED_MESSAGE_BEGIN = "filtered because not (";
    /**
     * Suffix for exported filter reasons.
     */
    public static final String FILTERED_MESSAGE_END = ")";

    /**
     * Inplace Semigroup to compose to summaries.
     * The individual {@link ExplainedFilter.Explanation}s will be composed by their {@link ExplainedFilter.Explanation#ISEMIGROUP}.
     */
    public static final InplaceSemigroup<ExplainedFilterSummary> ISEMIGROUP =
            (a, b) -> MergeMap.putAllValues(
                    a.explanations,
                    b.explanations,
                    ExplainedFilter.Explanation.ISEMIGROUP
            );

    private final LinkedHashMap<String, ExplainedFilter.Explanation> explanations;

    /**
     * Creates a new empty summary.
     */
    public ExplainedFilterSummary() {
        this.explanations = new LinkedHashMap<>();
    }

    /**
     * Creates a new summary that summarizes the current state of a given explained filter.
     * @param filter Filter whose hits should be memorized.
     * @param <T> The type of filtered values.
     */
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
    
    /**
     * Parses lines containing {@link ExplainedFilter.Explanation Explanations} to {@link ExplainedFilterSummary}.
     * 
     * @param lines Lines containing {@link ExplainedFilter.Explanation Explanations} to be parsed
     * @return {@link ExplainedFilterSummary}
     */
    public static ExplainedFilterSummary parse(final List<String> lines) {
        ExplainedFilterSummary summary = new ExplainedFilterSummary();
        String[] keyValuePair;
        String key;
        int value;
        for (final String line : lines) {
            keyValuePair = line.split(": ");
            key = keyValuePair[0];
            key = key.substring(FILTERED_MESSAGE_BEGIN.length(), key.length() - FILTERED_MESSAGE_END.length());
            value = Integer.parseInt(keyValuePair[1]);
            
            // create explanation
            ExplainedFilter.Explanation explanation = new ExplainedFilter.Explanation(value, key);
            
            // add explanation
            summary.explanations.put(key, explanation);
        }
        return summary;
    }

    @Override
    public LinkedHashMap<String, Integer> snapshot() {
        return Functjonal.bimap(
                explanations,
                name -> FILTERED_MESSAGE_BEGIN + name + FILTERED_MESSAGE_END,
                ExplainedFilter.Explanation::getFilterCount,
                LinkedHashMap::new
        );
    }

    @Override
    public InplaceSemigroup<ExplainedFilterSummary> semigroup() {
        return ISEMIGROUP;
    }
}

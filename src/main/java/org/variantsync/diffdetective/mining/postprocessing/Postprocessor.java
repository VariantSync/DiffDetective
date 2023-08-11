package org.variantsync.diffdetective.mining.postprocessing;

import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.filter.VariationDiffFilter;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.filter.TaggedPredicate;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.transform.VariationDiffTransformer;

import java.util.List;
import java.util.Map;

/**
 * Generic Postprocessor for mined patterns.
 * Patterns are represented as VariationDiffs and might be filtered or transformed.
 */
public class Postprocessor<L extends Label> {
    private final List<VariationDiffTransformer<L>> transformers;
    private final ExplainedFilter<VariationDiff<L>> filters;

    /**
     * Result type for prostprocessing.
     * It contains the actual result, the list of processed trees, as well as metadata.
     * The filterCounts document which filter caused how many variation diffs to be filtered out.
     * Notice, that filters were ordered and when a filter was applied, subsequent filters were not tested.
     * Thus, each filter operated on the unfiltered trees of the previous filter.
     */
    public record Result<L extends Label>(List<VariationDiff<L>> processedTrees, Map<String, Integer> filterCounts) {}

    private Postprocessor(
            final List<VariationDiffTransformer<L>> transformers,
            final List<TaggedPredicate<String, ? super VariationDiff<L>>> namedFilters) {
        this.transformers = transformers;
        this.filters = new ExplainedFilter<VariationDiff<L>>(namedFilters.stream());
    }

    /**
     * Creates the default filter to distill semantic patterns from frequent subgraphs.
     * This processor will
     *   - filter ill-formed trees
     *   - filter trees with less than two edit classes
     *   - filter duplicates w.r.t. isomorphism
     *   - {@link CutNonEditedSubtrees}
     * @return the default postprocessor.
     */
    public static <L extends Label> Postprocessor<L> Default() {
        return new Postprocessor<>(
                List.of(new CutNonEditedSubtrees<>()),
                List.of(
                        // Filter ill-formed patterns
                        VariationDiffFilter.consistent(),
                        VariationDiffFilter.moreThanOneArtifactNode(),
                        VariationDiffFilter.hasAtLeastOneEditToVariability()
                )
        );
    }

    /**
     * Performs the postprocessing described by this Postprocessor object on the list of subgraphs.
     * To that end, all filters and transformers will be applied.
     * @param frequentSubgraphs A list of subgraphs to which to apply the postprocessing.
     * @return The processed variation diffs as well as some metadata.
     */
    public Result<L> postprocess(final List<VariationDiff<L>> frequentSubgraphs) {
        final List<VariationDiff<L>> processedTrees = frequentSubgraphs.stream()
                .filter(filters)
                .peek(tree -> VariationDiffTransformer.apply(transformers, tree))
                .toList();

        final Map<String, Integer> filterCounts = new ExplainedFilterSummary(filters).snapshot();
        return new Result<>(processedTrees, filterCounts);
    }
}

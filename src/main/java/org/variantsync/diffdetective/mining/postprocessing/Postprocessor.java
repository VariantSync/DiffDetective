package org.variantsync.diffdetective.mining.postprocessing;

import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.filter.TaggedPredicate;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.transform.DiffTreeTransformer;

import java.util.List;
import java.util.Map;

/**
 * Generic Postprocessor for mined patterns.
 * Patterns are represented as DiffTrees and might be filtered or transformed.
 */
public class Postprocessor<L extends Label> {
    private final List<DiffTreeTransformer<L>> transformers;
    private final ExplainedFilter<DiffTree<L>> filters;

    /**
     * Result type for prostprocessing.
     * It contains the actual result, the list of processed trees, as well as metadata.
     * The filterCounts document which filter caused how many difftrees to be filtered out.
     * Notice, that filters were ordered and when a filter was applied, subsequent filters were not tested.
     * Thus, each filter operated on the unfiltered trees of the previous filter.
     */
    public record Result<L extends Label>(List<DiffTree<L>> processedTrees, Map<String, Integer> filterCounts) {}

    private Postprocessor(
            final List<DiffTreeTransformer<L>> transformers,
            final List<TaggedPredicate<String, ? super DiffTree<L>>> namedFilters) {
        this.transformers = transformers;
        this.filters = new ExplainedFilter<DiffTree<L>>(namedFilters.stream());
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
                        DiffTreeFilter.consistent(),
                        DiffTreeFilter.moreThanOneArtifactNode(),
                        DiffTreeFilter.hasAtLeastOneEditToVariability()
                )
        );
    }

    /**
     * Performs the postprocessing described by this Postprocessor object on the list of subgraphs.
     * To that end, all filters and transformers will be applied.
     * @param frequentSubgraphs A list of subgraphs to which to apply the postprocessing.
     * @return The processed difftrees as well as some metadata.
     */
    public Result<L> postprocess(final List<DiffTree<L>> frequentSubgraphs) {
        final List<DiffTree<L>> processedTrees = frequentSubgraphs.stream()
                .filter(filters)
                .peek(tree -> DiffTreeTransformer.apply(transformers, tree))
                .toList();

        final Map<String, Integer> filterCounts = new ExplainedFilterSummary(filters).snapshot();
        return new Result<>(processedTrees, filterCounts);
    }
}

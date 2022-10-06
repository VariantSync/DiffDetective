package org.variantsync.diffdetective.mining.postprocessing;

import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.filter.DiffTreeFilter;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.filter.TaggedPredicate;
import org.variantsync.diffdetective.diff.difftree.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;

import java.util.List;
import java.util.Map;

/**
 * Generic Postprocessor for mined patterns.
 * Patterns are represented as DiffTrees and might be filtered or transformed.
 */
public class Postprocessor {
    private final List<DiffTreeTransformer> transformers;
    private final ExplainedFilter<DiffTree> filters;

    /**
     * Result type for prostprocessing.
     * It contains the actual result, the list of processed trees, as well as metadata.
     * The filterCounts document which filter caused how many difftrees to be filtered out.
     * Notice, that filters were ordered and when a filter was applied, subsequent filters were not tested.
     * Thus, each filter operated on the unfiltered trees of the previous filter.
     */
    public record Result(List<DiffTree> processedTrees, Map<String, Integer> filterCounts) {}

    private Postprocessor(
            final List<DiffTreeTransformer> transformers,
            final List<TaggedPredicate<String, DiffTree>> namedFilters) {
        this.transformers = transformers;
        this.filters = new ExplainedFilter<>(namedFilters.stream());
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
    public static Postprocessor Default() {
        return new Postprocessor(
                List.of(new CutNonEditedSubtrees()),
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
    public Result postprocess(final List<DiffTree> frequentSubgraphs) {
        final List<DiffTree> processedTrees = frequentSubgraphs.stream()
                .filter(filters)
                .peek(tree -> DiffTreeTransformer.apply(transformers, tree))
                .toList();

        final Map<String, Integer> filterCounts = new ExplainedFilterSummary(filters).snapshot();
        return new Result(processedTrees, filterCounts);
    }
}

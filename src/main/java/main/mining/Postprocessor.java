package main.mining;

import diff.difftree.DiffTree;
import diff.difftree.filter.DiffTreeFilter;
import diff.difftree.filter.DuplicateDiffTreeFilter;
import diff.difftree.transform.CutNonEditedSubtrees;
import diff.difftree.transform.DiffTreeTransformer;
import util.TaggedPredicate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic Postprocessor for mined patterns.
 * Patterns are represented as DiffTrees and might be filtered or transformed.
 */
public class Postprocessor {
    private static class FilterMetadata {
        int filterCount;
        String name;

        public FilterMetadata(int filterCount, String name) {
            this.filterCount = filterCount;
            this.name = name;
        }

        public void hit() {
            ++filterCount;
        }

        static FilterMetadata fromName(String name) {
            return new FilterMetadata(0, name);
        }
    }

    private final List<DiffTreeTransformer> transformers;
    private final List<TaggedPredicate<FilterMetadata, DiffTree>> filters;
    private final boolean removeDuplicates;

    /**
     * Result type for prostprocessing.
     * It contains the actual result, the list of processed trees, as well as metadata.
     * The filterCounts document which filter caused how many difftrees to be filtered out.
     * Notice, that filters were ordered and when a filter was applied, subsequent filters were not tested.
     * Thus, each filter operated on the unfiltered trees of the previous filter.
     */
    public record Result(List<DiffTree> processedTrees, Map<String, Integer> filterCounts) {}

    private Postprocessor(List<DiffTreeTransformer> transformers, List<TaggedPredicate<String, DiffTree>> namedFilters, boolean removeDuplicates) {
        this.transformers = transformers;
        this.filters = namedFilters.stream().map(
                filter -> filter.map(FilterMetadata::fromName)
        ).collect(Collectors.toList());
        this.removeDuplicates = removeDuplicates;
    }

    /**
     * Creates the default filter to distill semantic patterns from frequent subgraphs.
     * This processor will
     *   - filter ill-formed trees
     *   - filter trees with less than two atomic patterns
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
                        // condition patterns containing less than two atomic patterns
                        DiffTreeFilter.moreThanTwoAtomicPatternsAfterMining()
                ),
                false
        );
    }

    /**
     * Performs the postprocessing described by this Postprocessor object on the list of subgraphs.
     * To that end, all filters and transformers will be applied.
     * @param frequentSubgraphs A list of subgraphs to which to apply the postprocessing.
     * @return The processed difftrees as well as some metadata.
     */
    public Result postprocess(final List<DiffTree> frequentSubgraphs) {
        List<DiffTree> processedTrees = frequentSubgraphs.stream()
                .filter(tree -> {
                    for (final TaggedPredicate<FilterMetadata, DiffTree> filter : filters) {
                        if (!filter.condition().test(tree)) {
                            filter.tag().hit();
                            return false;
                        }
                    }

                    return true;
                })
                .peek(tree -> DiffTreeTransformer.apply(transformers, tree))
                .toList();

        final Map<String, Integer> filterCounts = filters.stream().collect(Collectors.toMap(
                f -> f.tag().name,
                f -> f.tag().filterCount
        ));

        if (removeDuplicates) {
            final int patternsBefore = processedTrees.size();
            processedTrees = DuplicateDiffTreeFilter.wrtIsomorphism().filterDuplicates(processedTrees);
            final int numDuplicates = patternsBefore - processedTrees.size();
            filterCounts.put("Duplicate", numDuplicates);
        }

        return new Result(processedTrees, filterCounts);
    }
}

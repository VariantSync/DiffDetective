package mining;

import diff.difftree.DiffTree;
import diff.difftree.analysis.DiffTreeStatistics;
import diff.difftree.transform.CutNonEditedSubtrees;
import diff.difftree.transform.DiffTreeTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic Postprocessor for mined patterns.
 * Patterns are represented as DiffTrees and might be filtered or transformed.
 */
public class Postprocessor {
    private final List<DiffTreeTransformer> transformers;
    private final List<PatternFilter> filters;
    private final boolean removeDuplicates;

    /**
     * Result type for prostprocessing.
     * It contains the actual result, the list of processed trees, as well as metadata.
     * The filterCounts document which filter caused how many difftrees to be filtered out.
     * Notice, that filters were ordered and when a filter was applied, subsequent filters were not tested.
     * Thus, each filter operated on the unfiltered trees of the previous filter.
     */
    public static record Result(List<DiffTree> processedTrees, Map<String, Integer> filterCounts) {}

    private Postprocessor(List<DiffTreeTransformer> transformers, List<PatternFilter> filters, boolean removeDuplicates) {
        this.transformers = transformers;
        this.filters = filters;
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
                        new PatternFilter("Ill-Formed", tree -> tree.isConsistent().isSuccess()),
                        // condition patterns containing less than two atomic patterns
                        new PatternFilter("Less than two atomics", tree -> DiffTreeStatistics.getNumberOfUniqueLabelsIn(tree) > 1)
                ),
                true
        );
    }

    /**
     * Performs the postprocessing described by this Postprocessor object on the list of subgraphs.
     * To that end, all filters and transformers will be applied.
     * @param frequentSubgraphs A list of subgraphs to which to apply the postprocessing.
     * @return The processed difftrees as well as some metadata.
     */
    public Result postprocess(final List<DiffTree> frequentSubgraphs) {
        final Map<String, Integer> filterCounts = new HashMap<>();
        for (final PatternFilter filter : filters) {
            filterCounts.put(filter.name(), 0);
        }

        List<DiffTree> processedTrees = frequentSubgraphs.stream()
                .filter(tree -> {
                    for (final PatternFilter filter : filters) {
                        if (!filter.condition().test(tree)) {
                            filterCounts.computeIfPresent(filter.name(), (name, i) -> i + 1);
                            return false;
                        }
                    }

                    return true;
                })
                .peek(tree -> DiffTreeTransformer.apply(transformers, tree))
                .toList();

        if (removeDuplicates) {
            final int patternsBefore = processedTrees.size();
            processedTrees = DuplicatePatternFilter.filterDuplicates(processedTrees);
            final int numDuplicates = patternsBefore - processedTrees.size();
            filterCounts.put("Duplicate", numDuplicates);
        }

        return new Result(processedTrees, filterCounts);
    }
}

package diff.difftree.analysis;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import pattern.atomic.AtomicPatternCatalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DiffTreeStatistics {
    public static long getNumberOfUniqueLabelsOfNodes(final DiffTree t, final Predicate<DiffNode> shouldIncludeLabel) {
        final List<String> labels = new ArrayList<>();
        t.forAll(n -> {
            if (shouldIncludeLabel.test(n)) {
                labels.add(n.getLabel());
            }
        });
        return labels
                .stream()
                .distinct()
                .count();
    }

    public static long getNumberOfUniqueAtomicPatternsIn(final DiffTree t, final AtomicPatternCatalogue patterns) {
        final Map<String, Boolean> atomicPatternNames = new HashMap<>();
        t.forAll(n -> {
            if (n.isCode()) {
                atomicPatternNames.putIfAbsent(patterns.match(n).getName(), Boolean.TRUE);
            }
        });
        return atomicPatternNames.size();
    }
}

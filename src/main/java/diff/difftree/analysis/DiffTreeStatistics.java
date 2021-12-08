package diff.difftree.analysis;

import diff.difftree.DiffTree;
import pattern.AtomicPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffTreeStatistics {
    public static long getNumberOfUniqueLabelsIn(final DiffTree t) {
        final List<String> labels = new ArrayList<>();
        t.forAll(n -> labels.add(n.getLabel()));
        return labels
                .stream()
                .distinct()
                .count();
    }

    public static long getNumberOfUniqueAtomicPatternsIn(final DiffTree t) {
        final Map<String, Boolean> atomicPatternNames = new HashMap<>();
        t.forAll(n -> {
            if (n.isCode()) {
                atomicPatternNames.putIfAbsent(AtomicPattern.getPattern(n).getName(), Boolean.TRUE);
            }
        });
        return atomicPatternNames.size();
    }
}

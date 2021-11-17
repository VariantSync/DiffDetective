package diff.difftree.analysis;

import diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.List;

public class DiffTreeStatistics {
    public static long getNumberOfUniqueLabelsIn(final DiffTree t) {
        final List<String> atomicPatternNames = new ArrayList<>();
        t.forAll(n -> atomicPatternNames.add(n.getLabel()));
        return atomicPatternNames
                .stream()
                .distinct()
                .count();
    }
}

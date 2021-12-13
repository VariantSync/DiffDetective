package pattern.atomic.proposed;

import diff.difftree.DiffType;
import pattern.atomic.AtomicPattern;
import pattern.atomic.AtomicPatternCatalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProposedAtomicPatterns implements AtomicPatternCatalogue {
    public static final AtomicPattern AddToPC = new AddToPC();
    public static final AtomicPattern AddWithMapping = new AddWithMapping();
    public static final AtomicPattern RemFromPC = new RemFromPC();
    public static final AtomicPattern RemWithMapping = new RemWithMapping();
    public static final AtomicPattern Specialization = new Specialization();
    public static final AtomicPattern Generalization = new Generalization();
    public static final AtomicPattern Reconfiguration = new Reconfiguration();
    public static final AtomicPattern Refactoring = new Refactoring();

    public static final List<AtomicPattern> All = List.of(
            AddToPC, AddWithMapping,
            RemFromPC, RemWithMapping,
            Specialization, Generalization, Reconfiguration, Refactoring
    );

    public static final Map<DiffType, List<AtomicPattern>> PatternsByType;

    public static final ProposedAtomicPatterns Instance = new ProposedAtomicPatterns();

    static {
        PatternsByType = new HashMap<>();
        for (final AtomicPattern ap : All) {
            PatternsByType.computeIfAbsent(ap.getDiffType(), d -> new ArrayList<>()).add(ap);
        }
    }

    private ProposedAtomicPatterns() {}

    @Override
    public List<AtomicPattern> all() {
        return All;
    }

    @Override
    public Map<DiffType, List<AtomicPattern>> byType() {
        return PatternsByType;
    }
}

package pattern.atomic.proposed;

import analysis.SAT;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import org.prop4j.Node;
import pattern.atomic.AtomicPattern;
import pattern.atomic.AtomicPatternCatalogue;
import util.Assert;

import java.util.*;

public class ProposedAtomicPatterns implements AtomicPatternCatalogue {
    public static final AtomicPattern AddToPC = new AddToPC();
    public static final AtomicPattern AddWithMapping = new AddWithMapping();
    public static final AtomicPattern RemFromPC = new RemFromPC();
    public static final AtomicPattern RemWithMapping = new RemWithMapping();
    public static final AtomicPattern Specialization = new Specialization();
    public static final AtomicPattern Generalization = new Generalization();
    public static final AtomicPattern Reconfiguration = new Reconfiguration();
    public static final AtomicPattern Refactoring = new Refactoring();
    public static final AtomicPattern Unchanged = new Unchanged();

    public static final List<AtomicPattern> All = List.of(
            AddToPC, AddWithMapping,
            RemFromPC, RemWithMapping,
            Specialization, Generalization, Reconfiguration, Refactoring, Unchanged
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

    @Override
    public AtomicPattern match(DiffNode node)
    {
        // This is an inlined version of all patterns to optimize runtime when detecting the pattern of a certain node.

        // Because this compiles, we know that each branch terminates and returns a value.
        // Each returned value is not null but an actual pattern object.
        // Since the given node may be any node, we have proven that every node is classified by at least one pattern.
        if (!node.isCode()) {
            throw new IllegalArgumentException("Expected a code node but got " + node.codeType + "!");
        }

        if (node.isAdd()) {
            if (node.getAfterParent().isAdd()) {
                return AddWithMapping;
            } else {
                return AddToPC;
            }
        } else if (node.isRem()) {
            if (node.getBeforeParent().isRem()) {
                return RemWithMapping;
            } else {
                return RemFromPC;
            }
        } else {
            Assert.assertTrue(node.isNon());

            final Node pcb = node.getBeforePresenceCondition();
            final Node pca = node.getAfterPresenceCondition();

            /// We can avoid any SAT calls in case both formulas are syntactically equal.
            if (pcb.equals(pca)) {
                return Unchanged;
            }

            final boolean beforeVariantsSubsetOfAfterVariants = SAT.implies(pcb, pca);
            final boolean afterVariantsSubsetOfBeforeVariants = SAT.implies(pca, pcb);

//            System.out.println("Found NON node " + node.getLabel());
//            System.out.println("TAUT(" + pcb + " => " + pca + ") = " + beforeVariantsSubsetOfAfterVariants);
//            System.out.println("TAUT(" + pca + " => " + pcb + ") = " + afterVariantsSubsetOfBeforeVariants);

            // If the set of variants stayed the same.
            if (beforeVariantsSubsetOfAfterVariants && afterVariantsSubsetOfBeforeVariants) {
                if (node.beforePathEqualsAfterPath()) {
                    return Unchanged;
                } else {
                    return Refactoring;
                }
            }
            // If the set of variants grew.
            if (beforeVariantsSubsetOfAfterVariants) { // && !afterVariantsSubsetOfBeforeVariants
                return Generalization;
            }
            // If the set of variants shrank.
            if (afterVariantsSubsetOfBeforeVariants) { // && !beforeVariantsSubsetOfAfterVariants
                return Specialization;
            }

            // If the set of variants changed but there is no subset relation.
            // !beforeVariantsSubsetOfAfterVariants && !afterVariantsSubsetOfBeforeVariants
            return Reconfiguration;
        }
    }

    public Optional<AtomicPattern> fromName(String label) {
        for (final AtomicPattern p : All) {
            if (p.getName().equals(label)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}

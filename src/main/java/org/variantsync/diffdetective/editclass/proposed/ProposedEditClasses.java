package org.variantsync.diffdetective.editclass.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;

import java.util.*;

/**
 * The catalog of edit classes proposed in our ESEC/FSE'22 paper.
 * @author Paul Bittner
 */
public class ProposedEditClasses implements EditClassCatalogue {
    public static final EditClass AddToPC = new AddToPC();
    public static final EditClass AddWithMapping = new AddWithMapping();
    public static final EditClass RemFromPC = new RemFromPC();
    public static final EditClass RemWithMapping = new RemWithMapping();
    public static final EditClass Specialization = new Specialization();
    public static final EditClass Generalization = new Generalization();
    public static final EditClass Reconfiguration = new Reconfiguration();
    public static final EditClass Refactoring = new Refactoring();
    public static final EditClass Untouched = new Untouched();

    /**
     * A list of all nine edit classes in their order of appearance in the paper.
     */
    public static final List<EditClass> All = List.of(
            AddToPC, AddWithMapping,
            RemFromPC, RemWithMapping,
            Specialization, Generalization, Reconfiguration, Refactoring, Untouched
    );

    /**
     * A map of all nine edit classes, indexed by their DiffType.
     */
    public static final Map<DiffType, List<EditClass>> EditClassesByType;

    /**
     * Singleton instance of this catalog.
     */
    public static final ProposedEditClasses Instance = new ProposedEditClasses();

    static {
        EditClassesByType = new HashMap<>();
        for (final EditClass ap : All) {
            EditClassesByType.computeIfAbsent(ap.getDiffType(), d -> new ArrayList<>()).add(ap);
        }
    }

    private ProposedEditClasses() {}

    @Override
    public List<EditClass> all() {
        return All;
    }

    @Override
    public Map<DiffType, List<EditClass>> byType() {
        return EditClassesByType;
    }

    @Override
    public EditClass match(DiffNode node)
    {
        // This is an inlined version of all edit classes to optimize runtime when detecting the class of a certain node.

        // Because this compiles, we know that each branch terminates and returns a value.
        // Each returned value is not null but an actual edit class object.
        // Since the given node may be any node, we have proven that every node is classified by at least one edit class.
        if (!node.isArtifact()) {
            throw new IllegalArgumentException("Expected an artifact node but got " + node.nodeType + "!");
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

            final boolean beforeVariantsSubsetOfAfterVariants;
            final boolean afterVariantsSubsetOfBeforeVariants;

            /// We can avoid any SAT calls in case both formulas are syntactically equal.
            if (pcb.equals(pca)) {
                beforeVariantsSubsetOfAfterVariants = true;
                afterVariantsSubsetOfBeforeVariants = true;
            } else {
                beforeVariantsSubsetOfAfterVariants = SAT.implies(pcb, pca);
                afterVariantsSubsetOfBeforeVariants = SAT.implies(pca, pcb);
            }

//            System.out.println("Found NON node " + node.getLabel());
//            System.out.println("TAUT(" + pcb + " => " + pca + ") = " + beforeVariantsSubsetOfAfterVariants);
//            System.out.println("TAUT(" + pca + " => " + pcb + ") = " + afterVariantsSubsetOfBeforeVariants);

            // If the set of variants stayed the same.
            if (beforeVariantsSubsetOfAfterVariants && afterVariantsSubsetOfBeforeVariants) {
                if (node.beforePathEqualsAfterPath()) {
                    return Untouched;
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

    /**
     * Returns the edit class that has the given name.
     * Returns empty of no edit class has the given name.
     */
    public Optional<EditClass> fromName(String label) {
        for (final EditClass p : All) {
            if (p.getName().equals(label)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}

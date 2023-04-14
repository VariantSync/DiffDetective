package org.variantsync.diffdetective.analysis.logic;

import org.prop4j.And;
import org.prop4j.Node;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.util.fide.FormulaUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree;

import java.util.*;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;
import static org.variantsync.diffdetective.util.fide.FormulaUtils.removeSemanticDuplicates;

public class UniqueViewsAlgorithm {
    private static final int POWERSET_BLOWUP_THRESHOLD = 8;
    static {
        assert POWERSET_BLOWUP_THRESHOLD < Integer.SIZE;
    }

    /**
     * Build a set of partial configurations such that
     * - every config denotes a view of the given diff
     * - every view is uniqe
     * - every possible view is included
     * This works by deselecting any subset of presence conditions of the artifacts in the given diff.
     * @param d
     * @param simplify Whether to simplify formulas in between the algorithm.
     * @return
     */
    public static List<Node> getUniquePartialConfigs(DiffTree d, boolean simplify) {
        final Set<Node> deselectedPCs = new LinkedHashSet<>();

        // Collect all PCs negated
        d.forAll(a -> {
            if (a.isArtifact() && !ProposedEditClasses.Untouched.matches(a)) { // remove second clause for variation trees
                a.getDiffType().forAllTimesOfExistence(t -> {
                    Node deselectedPC = a.getPresenceCondition(t);

                    deselectedPC = FixTrueFalse.EliminateTrueAndFalseInplace(deselectedPC); // must
                    deselectedPC = negate(deselectedPC); // must
                    deselectedPC = deselectedPC.toRegularCNF(simplify); // optimization
                    FormulaUtils.sortRegularCNF(deselectedPC); // optimization

                    deselectedPCs.add(deselectedPC);
                });
            }
        });

        // remove semantic duplicates
        final List<Node> deselectedPCsList = new ArrayList<>(deselectedPCs);
        removeSemanticDuplicates(deselectedPCsList);

        // Algorithm is restricted in number of different PCs it can handle.
        if (deselectedPCsList.size() > POWERSET_BLOWUP_THRESHOLD) {
            return null;
        }

        // Optimization Heuristic: Sort list of PCs
        deselectedPCsList.sort(Comparator
                .comparingInt((Node e) -> e.getChildren().length)
                .thenComparing(FormulaUtils::numberOfLiteralsInRegularCNF)
        );

        // powerset
        final int powsetSize = 1 << deselectedPCsList.size();

        final List<Node> partialConfigs = new ArrayList<>(powsetSize); // :-1: by Sebastian
        for (int pcsToDeselectBitVector = 0; pcsToDeselectBitVector < powsetSize; ++pcsToDeselectBitVector) {
            int pcsRemainingToDeselect = Integer.bitCount(pcsToDeselectBitVector);
            final List<Node> subset = new ArrayList<>(pcsRemainingToDeselect);

            // As long as there are more pcs to deselect, deselect the one at the current bit.
            for (int i = 0; pcsRemainingToDeselect > 0; ++i) {
                if (((1L << i) & pcsToDeselectBitVector) > 0) {
                    subset.add(deselectedPCsList.get(i));
                    --pcsRemainingToDeselect;
                }
            }

            final Node viewFormula = new And(subset).toCNF(simplify);
            if (SAT.isSatisfiable(viewFormula)) {
                partialConfigs.add(viewFormula);
            } else {
                // OPTIMIZATION
                // skip some of the unsatisfiable next cases that just add further clauses to our already
                // conflicting clause set
                pcsToDeselectBitVector += 1 << Integer.lowestOneBit(pcsToDeselectBitVector);
            }
        }

        // remove semantic duplicates
        removeSemanticDuplicates(partialConfigs);

        return partialConfigs;//*/
    }
}

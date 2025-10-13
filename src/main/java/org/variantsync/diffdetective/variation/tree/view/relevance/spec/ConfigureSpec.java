package org.variantsync.diffdetective.variation.tree.view.relevance.spec;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.util.fide.FixTrueFalse.Formula;
import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

/**
 * This class serves as a specification for {@link org.variantsync.diffdetective.variation.tree.view.relevance.Configure}.
 * Both classes must act semantically equivalent as a {@link Relevance} predicate.
 * Whereas Configure is an implementation optimized to avoid SAT calls where necessary, the implementation in ConfigureSpec
 * is kept simple by design to remain verifiable.
 * ConfigureSpec tests a partial configuration against a variation tree or diff by checking each node individually.
 * To this end ConfigureSpec uses the default, naive implementations of {@link Relevance} instead of providing
 * optimized implementations for tree traversal as Configure does.
 *
 * This class is not intended for production use and rather is supposed to be used in tests.
 *
 * @author Paul Bittner
 */
public record ConfigureSpec(Formula config) implements Relevance {
    public static boolean test(Formula formula, VariationNode<?, ?> v) {
        return SAT.isSatisfiable(
                Formula.and(
                        formula,
                        FixTrueFalse.EliminateTrueAndFalse(v.getPresenceCondition())
                )
        );
    }

    public ConfigureSpec(final Node configuration) {
        this(FixTrueFalse.EliminateTrueAndFalse(configuration));
    }

    @Override
    public boolean test(VariationNode<?, ?> t) {
        return test(config, t);
    }

    @Override
    public String getFunctionName() {
        return "configure_spec";
    }

    @Override
    public String parametersToString() {
        return config.get().toString(NodeWriter.logicalSymbols);
    }

    @Override
    public String toString() {
        return Relevance.toString(this);
    }
}

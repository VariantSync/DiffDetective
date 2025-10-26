
package org.variantsync.diffdetective.variation.diff.transform;

import org.prop4j.Node;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

import java.util.List;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.*;

/**
 * This transformer simplifies annotations such that empty alternatives do not appear in choices.
 * This means, that nestings without any siblings such as
 *
 * <pre>{@code
 * #if A
 * #elif B
 * #elif C
 * #else
 *   foo
 * #endif
 * }</pre>
 *
 * will be simplified to
 *
 * <pre>{@code
 * #if !A && !B && !C
 *   foo
 * #endif
 * }</pre>
 *
 * Annotations without any children also get eliminated.
 *
 * @author Paul Bittner
 */
public class EliminateEmptyAlternatives<L extends Label> implements Transformer<VariationTree<L>> {
    private void elim(VariationTreeNode<L> subtree) {
        // We simplify only annotations.
        if (!subtree.isAnnotation()) return;

        final List<VariationTreeNode<L>> children = subtree.getChildren();

        // When there are no children, 'subtree' is an empty annotation that can be eliminated.
        if (children.isEmpty()) {
            subtree.drop();
        }
        // When there is exactly one child and that child is an 'else' or 'elif' we can simplify that nesting.
        else if (children.size() == 1) {
            final VariationTreeNode<L> child = children.getFirst();

            if ((subtree.isIf() || subtree.isElif()) && (child.isElif() || child.isElse())) {
                // determine new feaure mapping
                Node newFormula = negate(subtree.getFormula());
                if (child.isElif()) {
                    newFormula = and(newFormula, child.getFormula());
                }
                subtree.setFormula(newFormula);

                // simplify tree
                child.drop();
                subtree.stealChildrenOf(child);
            }
        }
    }

    @Override
    public void transform(VariationTree<L> tree) {
        tree.forAllPostorder(this::elim);
    }
}

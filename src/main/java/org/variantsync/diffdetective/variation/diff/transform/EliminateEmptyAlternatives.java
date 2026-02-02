package org.variantsync.diffdetective.variation.diff.transform;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import static org.variantsync.diffdetective.variation.DiffLinesLabel.Line;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

import java.util.ArrayList;
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
public class EliminateEmptyAlternatives implements Transformer<VariationTree<DiffLinesLabel>> {
    /**
     * Creates a copy of the given label but where the formula is set to the given formula.
     * This method also updates the text in the DiffLinesLabel accordingly so that the text is
     * consistent with the formula.
     * This method assumes that the label has at least one line of text, otherwise the given label
     * could not have a formula.
     */
    private static DiffLinesLabel updatedLabel(DiffLinesLabel l, Node formula) {
        final List<Line> lines = l.getDiffLines();
        Assert.assertFalse(lines.isEmpty());

        // Assumption:
        // The only case in which there is more than one line of text is, when we parsed a multiline macro.
        //
        // We hence may safely ignore any subsequent lines from our existing label because these correspond
        // only to lines of a multiline macro, which we ought to replace anyway.
        final Line head = lines.get(0);
        Assert.assertTrue(head.content().contains("if"));
        final String indent = StringUtils.getLeadingWhitespace(head.content());

        final String newText = indent + "#if " + formula.toString(NodeWriter.javaSymbols);

        // We might have replaced multiple lines by a single line here.
        // In this case, some line numbers got lost and any variation tree using this updated label somewhere might not
        // have consecutive line numbering anymore. We could consider inserting empty lines to retain
        // consecutive line numbers but that might be a more artifical change than inconsecutive line numbers.
        return new DiffLinesLabel(
            List.of(new Line(newText, head.lineNumber())),
            l.getDiffTrailingLines()
        );
    }

    private static void elim(VariationTreeNode<DiffLinesLabel> subtree) {
        // We simplify only annotations.
        if (!subtree.isAnnotation()) return;

        final List<VariationTreeNode<DiffLinesLabel>> children = subtree.getChildren();

        // When there are no children, 'subtree' is an empty annotation that can be eliminated.
        if (children.isEmpty()) {
//            subtree.drop();
        	nodesToDrop.add(subtree);
        }
        // When there is exactly one child and that child is an 'else' or 'elif' we can simplify that nesting.
        else if (children.size() == 1) {
            final VariationTreeNode<DiffLinesLabel> child = children.getFirst();

            if ((subtree.isIf() || subtree.isElif()) && (child.isElif() || child.isElse())) {
                // determine new feaure mapping
                Node newFormula = negate(subtree.getFormula());
                if (child.isElif()) {
                    newFormula = and(newFormula, child.getFormula());
                }
                subtree.setFormula(newFormula);
                subtree.setLabel(updatedLabel(subtree.getLabel(), newFormula));

                // simplify tree
//                child.drop();
                nodesToDrop.add(child);
                subtree.stealChildrenOf(child);
            }
        }
    }

    @Override
    public void transform(VariationTree<DiffLinesLabel> tree) {
        tree.forAllPostorder(EliminateEmptyAlternatives::elim);
    }
}
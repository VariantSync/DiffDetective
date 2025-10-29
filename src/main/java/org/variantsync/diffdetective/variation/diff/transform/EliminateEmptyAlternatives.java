
package org.variantsync.diffdetective.variation.diff.transform;


import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.util.Assert;
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
    private static List<VariationTreeNode<DiffLinesLabel>> nodesToDrop = new ArrayList<>();
	public static String leadingWhitespace(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        int i = 0;
        for (; i < s.length() && Character.isWhitespace(s.charAt(i)); i++) {}
        return s.substring(0, i);
    }

    private static DiffLinesLabel updatedLabel(DiffLinesLabel l, Node formula) {
        final List<Line> lines = l.getDiffLines();
        Assert.assertFalse(lines.isEmpty());

        // Assumptions: There are only more than one line when the label represents a multiline macro.
        // We are interested only in the indentation of the CPP macro, so only need the first line.
        final Line head = lines.get(0);
        Assert.assertTrue(head.content().contains("if"));

        final String newText = leadingWhitespace(head.content()) + "#if " + formula.toString(NodeWriter.javaSymbols);
        return new DiffLinesLabel(List.of(new Line(newText, head.lineNumber())), l.getDiffTrailingLines());
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
            final VariationTreeNode<DiffLinesLabel> child = children.get(0);

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
        nodesToDrop.forEach(node -> node.drop());
        nodesToDrop.clear();
    }
}

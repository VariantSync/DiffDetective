package org.variantsync.diffdetective.variation;

import java.util.Stack;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

public class VariationUnparser {
    public static String variationTreeUnparser(VariationTree<? extends Label> tree) {
        StringBuilder result = new StringBuilder();
        Stack<VariationTreeNode<? extends Label>> stack = new Stack<>();
        for (int i = tree.root().getChildren().size() - 1; i >= 0; i--) {
            stack.push(tree.root().getChildren().get(i));
        }
        while (!stack.empty()) {
            VariationTreeNode<? extends Label> node = stack.pop();
            for (String line : node.getLabel().getLines()) {
                result.append(line);
                result.append("\n");
            }
            if (node.isIf()) {
                stack.push(new VariationTreeNode<>(NodeType.ARTIFACT, null, null,
                        DiffLinesLabel.withInvalidLineNumbers(node.getEndIf())));
            }
            for (int i = node.getChildren().size() - 1; i >= 0; i--) {
                stack.push(node.getChildren().get(i));
            }
        }
        return result.substring(0, result.length() - 1);
    }
}

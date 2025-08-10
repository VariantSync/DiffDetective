package org.variantsync.diffdetective.variation;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

public class VariationUnparser {
    /**
     * Unparse {@link VariationTree}s into a {@link String}.
     *
     * @param tree that is unparsed
     * @param linesToLabel a function that converts lists of lines into labels
     * @return the unparsed variation tree
     * @param <L> the type of labels of the tree
     */
    public static <L extends Label> String variationTreeUnparser(VariationTree<L> tree, Function<List<String>, L> linesToLabel) {
        if (!tree.root().getChildren().isEmpty()) {
            StringBuilder result = new StringBuilder();
            Stack<VariationTreeNode<L>> stack = new Stack<>();
            for (int i = tree.root().getChildren().size() - 1; i >= 0; i--) {
                stack.push(tree.root().getChildren().get(i));
            }
            while (!stack.empty()) {
                VariationTreeNode<L> node = stack.pop();
                if (node.isIf()) {
                    stack.push(new VariationTreeNode<>(NodeType.ARTIFACT, null, null,
                            linesToLabel.apply(node.getEndIf())));
                }
                for (String line : node.getLabel().getLines()) {
                    result.append(line);
                    result.append("\n");
                }
                for (int i = node.getChildren().size() - 1; i >= 0; i--) {
                    stack.push(node.getChildren().get(i));
                }
            }
            return result.substring(0, result.length() - 1);
        } else {
            return "";
        }
    }

    /**
     * Unparse {@link VariationTree}s into a {@link String}.
     *
     * @param tree that is unparsed
     * @param linesToLabel a function that converts lists of lines into labels
     * @return the unparsed variation tree
     */
    public static String variationTreeUnparser(VariationTree<DiffLinesLabel> tree) {
        return variationTreeUnparser(tree, DiffLinesLabel::withInvalidLineNumbers);
    }

    /**
     * Unparse {@link VariationDiff}s into a {@link String}.
     *
     * @param diff that is unparsed
     * @param linesToLabel a function that converts lists of lines into labels
     * @return the unparsed variation diff
     * @param <L> the type of labels of the tree
     * @throws IOException
     */
    public static <L extends Label> String variationDiffUnparser(VariationDiff<L> diff, Function<List<String>, L> linesToLabel) throws IOException {
        String tree1 = variationTreeUnparser(diff.project(Time.BEFORE), linesToLabel);
        String tree2 = variationTreeUnparser(diff.project(Time.AFTER), linesToLabel);
        return JGitDiff.textDiff(tree1, tree2, SupportedAlgorithm.MYERS);
    }

    /**
     * Unparse {@link VariationDiff}s into a {@link String}.
     *
     * @param diff that is unparsed
     * @return the unparsed variation diff
     * @throws IOException
     */
    public static String variationDiffUnparser(VariationDiff<DiffLinesLabel> diff) throws IOException {
        return variationDiffUnparser(diff, DiffLinesLabel::withInvalidLineNumbers);
    }

    /**
     * Extract the state of the diffed text before or after {@code diff}.
     *
     * @param diff the diff from which the state is extracted
     * @param time that the returned state represents
     * @return the state before or after the diff
     */
    public static String undiff(String diff, Time time) {
        String excludedDiffSymbol = DiffType.thatExistsOnlyAt(time.other()).symbol;

        String result = diff
            .lines()
            .filter(line -> !line.startsWith(excludedDiffSymbol))
            // TODO assumes that all diff symbols are exactly 1 char long
            .map(line -> line.isEmpty() ? "" : line.substring(1))
            .collect(Collectors.joining("\n"));

        return result.isEmpty() ? "" : result + "\n";
    }
}

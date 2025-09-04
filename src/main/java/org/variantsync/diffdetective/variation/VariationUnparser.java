package org.variantsync.diffdetective.variation;

import java.io.IOException;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.tree.VariationTree;

public class VariationUnparser {
    /**
     * Unparse {@link VariationTree}s into a {@link String}.
     *
     * @param tree that is unparsed
     * @return the unparsed variation tree
     * @param <L> the type of labels of the tree
     */
    public static <L extends Label> String unparseTree(VariationTree<L> tree) {
        return tree.unparse();
    }

    /**
     * Unparse {@link VariationDiff}s into a {@link String}.
     *
     * @param diff that is unparsed
     * @return the unparsed variation diff
     * @param <L> the type of labels of the tree
     * @throws IOException
     */
    public static <L extends Label> String unparseDiff(VariationDiff<L> diff) throws IOException {
        String tree1 = unparseTree(diff.project(Time.BEFORE));
        String tree2 = unparseTree(diff.project(Time.AFTER));
        return JGitDiff.textDiff(tree1, tree2, SupportedAlgorithm.MYERS);
    }

    /**
     * Extract the state of the diffed text before or after {@code diff}.
     *
     * @param diff the diff from which the state is extracted
     * @param time that the returned state represents
     * @return the state before or after the diff
     */
    public static String undiff(String diff, Time time) {
        char excludedDiffSymbol = DiffType.thatExistsOnlyAt(time.other()).symbol;

        String result = diff
            .lines()
            .filter(line -> line.isEmpty() || line.charAt(0) != excludedDiffSymbol)
            .map(line -> line.isEmpty() ? "" : line.substring(1))
            .collect(Collectors.joining("\n"));

        return result.isEmpty() ? "" : result + "\n";
    }
}

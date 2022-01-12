package mining;

import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.DiffTreeSource;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.transform.ExampleFinder;
import org.prop4j.Literal;
import org.prop4j.Node;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Assert;

import java.nio.file.Path;

public class RunningExampleFinder {
    private static final Path DefaultExamplesDirectory = Path.of("examples");
    private static final AtomicPattern AddWithMapping = ProposedAtomicPatterns.AddWithMapping;

    public static final ExampleFinder Default = new ExampleFinder(
            diffTree -> {
                // There should be an AddWithMapping.
                if (!AddWithMapping.anyMatch(diffTree)) {
                    return false;
                }

                // There should be nesting before the edit (else is also ok).
                if (!hasNestingBeforeEdit(diffTree)) {
                    return false;
                }

                // The diff should be small.
                final int diffMaxLineCount = 13;
                if (getNumberOfLinesInDiffOf(diffTree.getSource()) > diffMaxLineCount) {
                    return false;
                }

                // We want to have annotated code and not annotated macros.
                // This could maybe be addressed by our new issue.
                if (hasAnnotatedMacros(diffTree)) {
                    return false;
                }

                // We would like to have a complex formula in the tree (complex := not just a positive literal).
                if (!diffTree.anyMatch(n -> {
                    // and the formula should be visible before the edit
                    if (n.isMacro() && !n.isAdd()) {
                        return isComplexFormula(n.getDirectFeatureMapping());
                    }

                    return false;
                })) {
                    return false;
                }

                return true;
            },
            DefaultExamplesDirectory,
            DiffTreeRenderer.WithinDiffDetective()
    );

    private static boolean hasAnnotatedMacros(final DiffTree diffTree) {
        return diffTree.anyMatch(n -> n.isCode() && n.getLabel().trim().startsWith("#"));
    }

    private static boolean hasNestingBeforeEdit(final DiffTree diffTree) {
        return diffTree.anyMatch(n -> !n.isAdd() && n.getBeforeDepth() > 2);
    }

    private static int getNumberOfLinesInDiffOf(final DiffTreeSource source) {
        Assert.assertTrue(source instanceof PatchDiff);
        final PatchDiff diff = (PatchDiff) source;
        return diff.getFullDiff().trim().split("\\r?\\n").length;
    }

    private static boolean isComplexFormula(final Node formula) {
        if (formula instanceof Literal) {
            // if a mapping is a negative literal, we count it as complex
            return !((Literal) formula).positive;
        } else {
            return true;
        }
    }
}

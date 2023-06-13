package org.variantsync.diffdetective.examplesearch;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.filter.TaggedPredicate;

import java.nio.file.Path;

import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

public class ExampleCriterions {
    public static final Path DefaultExamplesDirectory = Path.of("examples");
    public static final int DefaultMaxDiffLineCount = 20;

    public static <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> HAS_A_COMPLEX_FORMULA_BEFORE_THE_EDIT() {
        return new TaggedPredicate<>("has a complex formula before edit", ExampleCriterions::hasAtLeastOneComplexFormulaBeforeTheEdit);
    }
    public static <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> DOES_NOT_CONTAIN_ANNOTATED_MACROS() {
        return new TaggedPredicate<>("has no annotated macros", t -> !ExampleCriterions.hasAnnotatedMacros(t));
    }
    public static <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> HAS_EDITED_ARTIFACTS() {
        return new TaggedPredicate<>("an artifact was edited", t -> t.anyMatch(n -> n.isArtifact() && !n.isNon()));
    }
    public static <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> HAS_ADDITIONS() {
        return new TaggedPredicate<>("has additions", t -> t.anyMatch(DiffNode::isAdd));
    }
    public static <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> HAS_NESTING_BEFORE_EDIT() {
        return new TaggedPredicate<>("has nesting before the edit", ExampleCriterions::hasNestingBeforeEdit);
    }

    public static final <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> HAS_ELSE() {
        return new TaggedPredicate<>(
            "has at least one ELSE node",
            t -> t.anyMatch(DiffNode::isElse)
        );
    }
    public static <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> MAX_LINE_COUNT(int n) {
        return new TaggedPredicate<>(
                "diff length <= " + n,
                t -> diffIsNotLongerThan(t, n)
        );
    }

    public static <L extends Label> TaggedPredicate<String, DiffTree<? extends L>> MIN_ANNOTATIONS(int n) {
        return new TaggedPredicate<>(
                "has at least " + n + " annotations",
                t -> t.computeAnnotationNodes().size() >= n
        );
    }

    public static final <L extends Label> ExplainedFilter<DiffTree<L>> DefaultExampleConditions() {
        return new ExplainedFilter<>(
            MAX_LINE_COUNT(DefaultMaxDiffLineCount),
            HAS_NESTING_BEFORE_EDIT(),
            HAS_ADDITIONS(),
            HAS_EDITED_ARTIFACTS(),
            DiffTreeFilter.hasAtLeastOneEditToVariability(),
            DiffTreeFilter.moreThanOneArtifactNode(),
            DOES_NOT_CONTAIN_ANNOTATED_MACROS(),
            HAS_A_COMPLEX_FORMULA_BEFORE_THE_EDIT()
        );
    }

    private static boolean diffIsNotLongerThan(final DiffTree<?> t, int maxLines) {
        return getNumberOfLinesIn(ExampleFinder.getDiff(t)) <= maxLines;
    }

    private static boolean hasAnnotatedMacros(final DiffTree<?> diffTree) {
        return diffTree.anyMatch(n -> n.isArtifact() && n.getLabel().toString().trim().startsWith("#"));
    }

    private static boolean hasNestingBeforeEdit(final DiffTree<?> diffTree) {
        return diffTree.anyMatch(n ->
                           !n.isAdd()
                        && n.getDepth(BEFORE) > 2
                        && !(n.getParent(BEFORE).isElse() || n.getParent(BEFORE).isElif())
        );
    }

    private static boolean hasAtLeastOneComplexFormulaBeforeTheEdit(final DiffTree<?> diffTree) {
        // We would like to have a complex formula in the tree (complex := not just a positive literal).
        return diffTree.anyMatch(n -> {
            // and the formula should be visible before the edit
            if (n.isAnnotation() && !n.isAdd()) {
                return isComplexFormula(n.getFormula());
            }

            return false;
        });
    }

    private static int getNumberOfLinesIn(final String text) {
        return (int)text.trim().lines().count();
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

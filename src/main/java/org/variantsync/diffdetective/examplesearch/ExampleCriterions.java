package org.variantsync.diffdetective.examplesearch;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.filter.TaggedPredicate;
import org.variantsync.diffdetective.variation.diff.filter.VariationDiffFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

public class ExampleCriterions {
    public static final Path DefaultExamplesDirectory = Path.of("examples");
    public static final int DefaultMaxDiffLineCount = 20;

    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> HAS_A_COMPLEX_FORMULA_BEFORE_THE_EDIT() {
        return new TaggedPredicate<>("has a complex formula before edit", ExampleCriterions::hasAtLeastOneComplexFormulaBeforeTheEdit);
    }
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> DOES_NOT_CONTAIN_ANNOTATED_MACROS() {
        return new TaggedPredicate<>("has no annotated macros", t -> !ExampleCriterions.hasAnnotatedMacros(t));
    }
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> HAS_EDITED_ARTIFACTS() {
        return new TaggedPredicate<>("an artifact was edited", t -> t.anyMatch(n -> n.isArtifact() && !n.isNon()));
    }
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> HAS_ADDITIONS() {
        return new TaggedPredicate<>("has additions", t -> t.anyMatch(DiffNode::isAdd));
    }
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> HAS_DELETIONS() {
        return new TaggedPredicate<>("has deletions", t -> t.anyMatch(DiffNode::isRem));
    }
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> HAS_NESTING_BEFORE_EDIT() {
        return new TaggedPredicate<>("has nesting before the edit", t -> t.anyMatch(n -> isNestedAt(n, BEFORE)));
    }
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> HAS_NESTING() {
        return new TaggedPredicate<>("has nesting", t -> t.anyMatch(ExampleCriterions::isNested));
    }

    public static final <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> HAS_ELSE() {
        return new TaggedPredicate<>(
            "has at least one ELSE node",
            t -> t.anyMatch(DiffNode::isElse)
        );
    }
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> MAX_LINE_COUNT(int n) {
        return new TaggedPredicate<>(
                "diff length <= " + n,
                t -> diffIsNotLongerThan(t, n)
        );
    }

    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> MIN_NODES_OF_TYPE(NodeType nt, int n) {
        return new TaggedPredicate<>(
                "has at least " + n + " nodes of type " + nt,
                t -> t.computeAllNodesThat(node -> nt.equals(node.getNodeType())).size() >= n
        );
    }

    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> MIN_ANNOTATIONS(int n) {
        return new TaggedPredicate<>(
                "has at least " + n + " annotations",
                t -> t.computeAnnotationNodes().size() >= n
        );
    }

    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> MIN_FEATURES(int n) {
        return new TaggedPredicate<>(
                "has edits below at least " + n + " syntactically different presence conditions",
                t -> t.computeAnnotationNodes()
                        .stream()
                        .flatMap(node -> {
                            if (node.isElse()) {
                                return Stream.of();
                            } else {
                                return node.getFormula().getLiterals().stream().map(l -> l.var.toString());
                            }
                        })
                        .collect(Collectors.toSet())
                        .size()
                        >= n
        );
    }

    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> MIN_PARALLEL_EDITS(int n) {
        return new TaggedPredicate<>(
                "has at least " + n + " edits next to each other",
                t -> {
                    VariationDiff<? extends L> copy = BadVDiff.fromGood(t).deepCopy().toGood();
                    CutNonEditedSubtrees.genericTransform(copy);
                    return copy.anyMatch(node ->
                            node
                                    .getAllChildrenStream()
                                    .filter(DiffNode::isAnnotation)
                                    .count()
                                    >= n);
                }
        );
    }

    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> MIN_CHANGES_TO_PCS(int n) {
        return new TaggedPredicate<>(
                "has edits below at least " + n + " syntactically different presence conditions",
                t -> t.computeAllNodesThat(
                        node -> node.isArtifact() && !ProposedEditClasses.Untouched.matches(node)
                ).size()
                >= n
        );
    }

    public static final <L extends Label> ExplainedFilter<VariationDiff<L>> DefaultExampleConditions() {
        return new ExplainedFilter<>(
            MAX_LINE_COUNT(DefaultMaxDiffLineCount),
            HAS_NESTING_BEFORE_EDIT(),
            HAS_ADDITIONS(),
            HAS_EDITED_ARTIFACTS(),
            VariationDiffFilter.hasAtLeastOneEditToVariability(),
            VariationDiffFilter.moreThanOneArtifactNode(),
            DOES_NOT_CONTAIN_ANNOTATED_MACROS(),
            HAS_A_COMPLEX_FORMULA_BEFORE_THE_EDIT()
        );
    }

    private static boolean diffIsNotLongerThan(final VariationDiff<?> t, int maxLines) {
        return getNumberOfLinesIn(ExampleFinder.getDiff(t)) <= maxLines;
    }

    private static boolean hasAnnotatedMacros(final VariationDiff<?> variationDiff) {
        return variationDiff.anyMatch(n -> n.isArtifact() && n.getLabel().toString().trim().startsWith("#"));
    }

    private static boolean isNestedAt(final DiffNode<?> n, Time t) {
        return n.getDiffType().existsAtTime(t)
                && n.getDepth(t) > 2
                && !(n.getParent(t).isElse() || n.getParent(t).isElif());
    }

    private static boolean isNested(final DiffNode<?> n) {
        return isNestedAt(n, BEFORE) || isNestedAt(n, AFTER);
    }

    private static boolean hasAtLeastOneComplexFormulaBeforeTheEdit(final VariationDiff<?> variationDiff) {
        // We would like to have a complex formula in the tree (complex := not just a positive literal).
        return variationDiff.anyMatch(n -> {
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

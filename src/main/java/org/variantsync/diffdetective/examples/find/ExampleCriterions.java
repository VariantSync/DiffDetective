package org.variantsync.diffdetective.examples.find;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.git.GitPatch;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.diff.text.TextBasedDiff;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.filter.TaggedPredicate;
import org.variantsync.diffdetective.variation.diff.render.DiffTreeRenderer;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;

import java.nio.file.Path;
import java.util.Optional;

import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

public class ExampleCriterions {
    public static final Path DefaultExamplesDirectory = Path.of("examples");
    public static final int DefaultMaxDiffLineCount = 20;

    public static final TaggedPredicate<String, DiffTree> HAS_A_COMPLEX_FORMULA_BEFORE_THE_EDIT = new TaggedPredicate<>("has a complex formula before edit", ExampleCriterions::hasAtLeastOneComplexFormulaBeforeTheEdit);
    public static final TaggedPredicate<String, DiffTree> DOES_NOT_CONTAIN_ANNOTATED_MACROS = new TaggedPredicate<>("has no annotated macros", t -> !ExampleCriterions.hasAnnotatedMacros(t));
    public static final TaggedPredicate<String, DiffTree> HAS_EDITED_ARTIFACTS = new TaggedPredicate<>("an artifact was edited", t -> t.anyMatch(n -> n.isArtifact() && !n.isNon()));
    public static final TaggedPredicate<String, DiffTree> HAS_ADDITIONS = new TaggedPredicate<>("has additions", t -> t.anyMatch(DiffNode::isAdd));
    public static final TaggedPredicate<String, DiffTree> HAS_NESTING_BEFORE_EDIT = new TaggedPredicate<>("has nesting before the edit", ExampleCriterions::hasNestingBeforeEdit);

    public static final TaggedPredicate<String, DiffTree> HAS_ELSE = new TaggedPredicate<>(
            "has at least one ELSE node",
            t -> t.anyMatch(DiffNode::isElse)
    );
    public static TaggedPredicate<String, DiffTree> MAX_LINE_COUNT(int n) {
        return new TaggedPredicate<>(
                "diff length <= " + n,
                t -> diffIsNotLongerThan(t, n)
        );
    }

    public static TaggedPredicate<String, DiffTree> MIN_ANNOTATIONS(int n) {
        return new TaggedPredicate<>(
                "has at least " + n + " annotations",
                t -> t.computeAnnotationNodes().size() >= n
        );
    }

    public static final ExplainedFilter<DiffTree> DefaultExampleConditions = new ExplainedFilter<>(
            MAX_LINE_COUNT(DefaultMaxDiffLineCount),
            HAS_NESTING_BEFORE_EDIT,
            HAS_ADDITIONS,
            HAS_EDITED_ARTIFACTS,
            DiffTreeFilter.hasAtLeastOneEditToVariability(),
            DiffTreeFilter.moreThanOneArtifactNode(),
            DOES_NOT_CONTAIN_ANNOTATED_MACROS,
            HAS_A_COMPLEX_FORMULA_BEFORE_THE_EDIT
    );

    private final CPPAnnotationParser annotationParser;

    public ExampleCriterions(final CPPAnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }

    public ExampleFinder The_Diff_Itself_Is_A_Valid_DiffTree_And(final ExplainedFilter<DiffTree> treeConditions)
    {
        return new ExampleFinder(
                diffTree -> {
                    try {
                        final String localDiff = getDiff(diffTree);
                        final DiffTree localTree = DiffTree.fromDiff(localDiff, true, true, annotationParser);
                        // Not every local diff can be parsed to a difftree because diffs are unaware of the underlying language (i.e., CPP).
                        // We want only running examples whose diffs describe entire diff trees for easier understanding.
                        if (treeConditions.test(localTree)) {
                            Assert.assertTrue(diffTree.getSource() instanceof GitPatch);
                            final GitPatch diffTreeSource = (GitPatch) diffTree.getSource();
                            localTree.setSource(diffTreeSource.shallowClone());
                            return Optional.of(localTree);
                        }
                        return Optional.empty();
                    } catch (DiffParseException e) {
                        return Optional.empty();
                    }
                },
                DiffTreeRenderer.WithinDiffDetective()
        );
    }

    private static boolean diffIsNotLongerThan(final DiffTree t, int maxLines) {
        return getNumberOfLinesIn(getDiff(t)) <= maxLines;
    }

    private static boolean hasAnnotatedMacros(final DiffTree diffTree) {
        return diffTree.anyMatch(n -> n.isArtifact() && n.getLabel().trim().startsWith("#"));
    }

    private static boolean hasNestingBeforeEdit(final DiffTree diffTree) {
        return diffTree.anyMatch(n ->
                           !n.isAdd()
                        && n.getDepth(BEFORE) > 2
                        && !(n.getParent(BEFORE).isElse() || n.getParent(BEFORE).isElif())
        );
    }

    private static boolean hasAtLeastOneComplexFormulaBeforeTheEdit(final DiffTree diffTree) {
        // We would like to have a complex formula in the tree (complex := not just a positive literal).
        return diffTree.anyMatch(n -> {
            // and the formula should be visible before the edit
            if (n.isAnnotation() && !n.isAdd()) {
                return isComplexFormula(n.getFormula());
            }

            return false;
        });
    }

    private static String getDiff(final DiffTree tree) {
        final DiffTreeSource source = tree.getSource();
        Assert.assertTrue(source instanceof TextBasedDiff);
        return ((TextBasedDiff) source).getDiff();
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

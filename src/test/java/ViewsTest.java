import static org.variantsync.diffdetective.util.fide.FormulaUtils.and;
import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;
import static org.variantsync.diffdetective.util.fide.FormulaUtils.var;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;
import org.variantsync.diffdetective.variation.tree.view.relevance.spec.ConfigureSpec;

/**
 * Tests for views on variation trees and diffs.
 */
public class ViewsTest {
    private static final Path
        resDir = Constants.RESOURCE_DIR.resolve("diffs").resolve("views"),
        treeDir = resDir.resolve("tree"), // directory of test cases for variation trees
        diffDir = resDir.resolve("diff")  // directory of test cases for variation diffs
        ;

    /**
     * This record holds the implementation of a relevance predicate and its specification.
     * Both predicates will be tested for semantic equivalence.
     * The idea is that the first relevance is an optimized implementation, whereas the second
     * relevance is an easy-to-understand and easy-to-verify but suboptimal implementation.
     **/
    private static record RelevanceSpec(Relevance impl, Relevance spec) {}

    // These classes are test cases for testing consistency between a relevance predicate's implementation and specification
    // on trees and diffs respectively.
    private static record TreeConsistencyTestCase(VariationTree<DiffLinesLabel> tree, List<RelevanceSpec> relevances) {}
    private static record DiffConsistencyTestCase(VariationDiff<DiffLinesLabel> diff, List<RelevanceSpec> relevances) {}

    /**
     * Creates a TreeConsistencyTestCase for the Configure relevance.
     * @param fileName The name of the file to parse as variation tree.
     * @param fs any number of formulas to test view generation with
     */
    private static TreeConsistencyTestCase treeConf(String fileName, Node... fs) throws IOException, DiffParseException {
        return new TreeConsistencyTestCase(
            VariationTree.fromFile(treeDir.resolve(fileName), VariationDiffParseOptions.Default),
            Arrays.stream(fs).map(f -> new RelevanceSpec(new Configure(f), new ConfigureSpec(f))).toList()
        );
    }

    /**
     * Creates a DiffConsistencyTestCase for the Configure relevance.
     * @param fileName The name of the file to parse as variation diff.
     * @param fs any number of formulas to test view generation with
     */
    private static DiffConsistencyTestCase diffConf(String fileName, Node... fs) throws IOException, DiffParseException {
        return new DiffConsistencyTestCase(
            VariationDiff.fromFile(diffDir.resolve(fileName), VariationDiffParseOptions.Default),
            Arrays.stream(fs).map(f -> new RelevanceSpec(new Configure(f), new ConfigureSpec(f))).toList()
        );
    }

    private static List<TreeConsistencyTestCase> treeConsistencyTestCases() throws IOException, DiffParseException {
        final Literal A = var("A"), B = var("B"), C = var("C"), D = var("D"), E = var("E");
        return List.of(
            treeConf("else.c", A, negate(A), C),
            treeConf("elif.c", A, negate(A), B, negate(B)),
            treeConf("elif-chain.c", negate(E), and(negate(A), negate(B), negate(C), negate(D)))
        );
    }

    public static List<DiffConsistencyTestCase> diffConsistencyTestCases() throws IOException, DiffParseException {
        List<DiffConsistencyTestCase> testCases = new ArrayList<>();

        // We reuse all test cases for variation trees for variation diffs.
        testCases.addAll(treeConsistencyTestCases().stream().map(
            treeTestCase -> new DiffConsistencyTestCase(treeTestCase.tree.toCompletelyUnchangedVariationDiff(), treeTestCase.relevances)).toList()
        );

        // TODO: Create some additional test cases targeted specifically at variation diffs.
        //       In these test cases, the projections should be different.

        return testCases;
    }

    /**
     * Tests that a {@link Relevance} predicate is consistent with its specification on all
     * supplied test cases.
     */
    @ParameterizedTest
    @MethodSource("treeConsistencyTestCases")
    public void consistencyOnTrees(TreeConsistencyTestCase t) {
        for (RelevanceSpec relevance : t.relevances) {
            final VariationTree<DiffLinesLabel> impl = TreeView.tree(t.tree, relevance.impl);
            final VariationTree<DiffLinesLabel> spec = TreeView.tree(t.tree, relevance.spec);

            // To compare the trees, we create a diff.
            // If there are no deleted or removed nodes, both trees are equal.
            final VariationDiff<DiffLinesLabel> d = VariationDiff.fromTrees(impl, spec);
            Assert.assertTrue(d.allMatch(DiffNode::isNon));
        }
    }

    /**
     * Tests that
     * (1) a {@link Relevance} predicate is consistent with its specification, and
     * (2) the optimized generation of views on variation diffs
     * ({@link DiffView#optimized(VariationDiff, Relevance)}) is consistent
     * with the naive implementation ({@link DiffView#naive(VariationDiff, Relevance)})
     * which rather serves as a specification.
     */
    @ParameterizedTest
    @MethodSource("diffConsistencyTestCases")
    public void consistencyOnDiffs(DiffConsistencyTestCase t) throws IOException, DiffParseException {
        for (RelevanceSpec relevance : t.relevances) {
            // All of these must be equal.
            final VariationDiff<DiffLinesLabel> naive_impl = DiffView.naive(t.diff, relevance.impl);
            final VariationDiff<DiffLinesLabel> naive_spec = DiffView.naive(t.diff, relevance.spec);
            final VariationDiff<DiffLinesLabel>   opt_impl = DiffView.optimized(t.diff, relevance.impl);
            final VariationDiff<DiffLinesLabel>   opt_spec = DiffView.optimized(t.diff, relevance.spec);

            // First, we test consistency among relevance implementation and relevance specification.
            Assert.assertTrue(naive_impl.isSameAsIgnoringLineNumbers(naive_spec));
            Assert.assertTrue(opt_impl.isSameAsIgnoringLineNumbers(opt_spec));

            // Second, assuming relevance consistency, we test consistency between optimized and naive view generation.
            // We get the last comparison Assert.assertTrue(opt_impl.isSameAsIgnoringLineNumbers(naive_impl)) by transitivity.
            Assert.assertTrue(opt_spec.isSameAsIgnoringLineNumbers(naive_spec));
        }
    }

    // TODO: Test cases where we check the result of computing a view against a predefined ground truth.
}

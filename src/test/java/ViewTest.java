import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.prop4j.*;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.logic.UniqueViewsAlgorithm;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.diff.view.ViewSource;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.query.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.*;

@Disabled
public class ViewTest {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("badvdiff");

    private static void showViews(
            DiffTree initialVDiff,
            Query query
    ) {
        // treeify
        final BadVDiff badDiff = BadVDiff.fromGood(initialVDiff);

        // create view
        final BadVDiff view = badDiff.deepCopy();
        TreeView.treeInline(view.diff(), query);

        // unify
        final DiffTree goodDiff = view.toGood();
        goodDiff.assertConsistency();

        GameEngine.showAndAwaitAll(
                Show.diff(initialVDiff, "initial edit e"),
                Show.baddiff(badDiff, "tree(e)"),
                Show.baddiff(view, "view(tree(e), " + query + ")"),
                Show.diff(goodDiff, "unify(view(tree(e), " + query + "))")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "emacsbug1"
    })
    void debugTest(String filename) throws IOException, DiffParseException {
        final String filenameWithEnding = filename + ".diff";
        final Path testfile = resDir.resolve(filenameWithEnding);
        Logger.debug("Testing " + testfile);
//        is(  /* Check both of the above conditions, for symbols.  */)
        final DiffTree D = DiffTree.fromFile(testfile, DiffTreeParseOptions.Default);
        D.assertConsistency();

        final Query debugQuery = new ArtifactQuery("  /* Check both of the above conditions, for symbols.  */");
        final var imp = DiffView.computeWhenNodesAreRelevant(D, debugQuery);
        Show.diff(DiffView.optimized(D, debugQuery, imp)).showAndAwait();
        Show.diff(DiffView.naive(D, debugQuery, imp)).showAndAwait();
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "diamond",
            "deep_insertion"
    })
    void test(String filename) throws IOException, DiffParseException {
        final String filenameWithEnding = filename + ".diff";
        final Path testfile = resDir.resolve(filenameWithEnding);
        Logger.debug("Testing " + testfile);

        // Load diff
        final DiffTree initialVDiff = DiffTree.fromFile(testfile, DiffTreeParseOptions.Default);
        initialVDiff.assertConsistency();

        List<Query> queries = List.of(
                new FeatureQuery("B"),
                new VariantQuery(negate(var("B"))),
                new ArtifactQuery("foo")
        );

        for (Query q : queries) {
            final var viewNodes = DiffView.computeWhenNodesAreRelevant(initialVDiff, q);

            GameEngine.showAndAwaitAll(
                    Show.diff(initialVDiff, "D = " + filenameWithEnding),
                    Show.diff(DiffView.naive(initialVDiff, q, viewNodes), "diff_naive(D, " + q + ")"),
                    Show.diff(DiffView.optimized(initialVDiff, q, viewNodes), "diff_smart(D, " + q + ")")
            );
        }

//        showViews(initialVDiff, new VariantQuery(new And(new Literal("X"))));
//        showViews(initialVDiff, new VariantQuery(new And(new Literal("Y"))));
//        showViews(initialVDiff, new VariantQuery(new And(negate(new Literal("X")))));
//        showViews(initialVDiff, new VariantQuery(new And(negate(new Literal("Y")))));
//        showViews(initialVDiff, new FeatureQuery("X"));
//        showViews(initialVDiff, new FeatureQuery("Y"));
//        showViews(initialVDiff, new ArtifactQuery("y"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "runningexampleInDomain",
    })
    void inspectRunningExample(String filename) throws IOException, DiffParseException {
        final Path testfile = resDir.resolve(filename + ".diff");

//        final Literal X = var("Mutable");
        final Literal featureRing = var("Ring");
        final Literal featureDoubleLink = var("DoubleLink");

        final DiffTree d = DiffTree.fromFile(testfile, DiffTreeParseOptions.Default);
        final VariationTree b = d.project(Time.BEFORE);
        final VariationTree a = d.project(Time.AFTER);

        // Queries of Listing 3 and 4
        final Query bobsQuery1 = new VariantQuery(and(negate(featureDoubleLink)));
        final Query charlottesQuery = new VariantQuery(negate(featureRing));

        // Figure 1
        GameEngine.showAndAwaitAll(
                Show.tree(b, "Figure 1: project(D, b)")
        );

        // Figure 2
        GameEngine.showAndAwaitAll(
                Show.diff(d, "Figure 2: D")
        );

        // Figure 3
        final VariantQuery configureExample1 = new VariantQuery(
                and(featureRing, /* FM = */ negate(new And(featureDoubleLink, featureRing)))
        );
        GameEngine.showAndAwaitAll(
                Show.tree(TreeView.tree(b, configureExample1), "Figure 3: view_{tree}(Figure 1, " + configureExample1 + ")")
        );

        // Figure 4
        final FeatureQuery traceYesExample1 = new FeatureQuery(
                featureDoubleLink.toString()
        );
        GameEngine.showAndAwaitAll(
                Show.tree(TreeView.tree(b, traceYesExample1), "Figure 4: view_{tree}(Figure 1, " + traceYesExample1 + ")")
        );

        // Figure 5
        GameEngine.showAndAwaitAll(
                Show.diff(DiffView.optimized(d, charlottesQuery), "Figure 5: view_{naive}(Figure 2, " + charlottesQuery + ")")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "2"
//            "runningexample",
////            "const",
////            "diamond",
////            "deep_insertion"
    })
    void testAllConfigs(String filename) throws IOException, DiffParseException {
        final Path testfile = resDir.resolve(filename + ".diff");
        final DiffTree d = DiffTree.fromFile(testfile, DiffTreeParseOptions.Default);

        final List<Node> configs = UniqueViewsAlgorithm.getUniquePartialConfigs(d, false);
        final List<DiffTree> views = new ArrayList<>();

        final StringBuilder str = new StringBuilder();
        for (int i = 0; i < configs.size(); ++i) {
            final Node config = configs.get(i);

            final Query q = new VariantQuery(config);
            final DiffTree view = DiffView.optimized(d, q);
            views.add(view);

            str
                    .append(" ")
                    .append(org.apache.commons.lang3.StringUtils.leftPad(
                            Integer.toString(i),
                            4)
                    )
                    .append(".) ")
                    .append(org.apache.commons.lang3.StringUtils.rightPad(
                            config.toString(NodeWriter.logicalSymbols),
                            40)
                    )
                    .append(" --- ")
                    .append(
                            view.computeArtifactNodes().stream()
                                    .map(a -> a.getLabel().lines().stream()
                                            .map(String::trim)
                                            .collect(Collectors.joining(StringUtils.LINEBREAK))
                                    )
                                    .collect(Collectors.toList()))
                    .append(StringUtils.LINEBREAK);
        }

        Logger.info("All unique partial configs:" + StringUtils.LINEBREAK + str);
        Show.diff(d, "D").show();
//        {
//            final Query q = new FeatureQuery("C");
//            Show.diff(DiffView.optimized(d, q), "view(D, " + q.getName() + ")").showAndAwait();
//        }

        for (int i = 0; i < configs.size(); ++i) {
            final DiffTree view = views.get(i);
            final Query       q = ((ViewSource) view.getSource()).q();
            Show.diff(view, i + ".) view(D, " + q + ")").showAndAwait();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "2",
            "runningexample",
            "const",
            "diamond",
            "deep_insertion"
    })
    void cutTest(String filename) throws IOException, DiffParseException {
        final Path testfile = resDir.resolve(filename + ".diff");
        final DiffTree d = DiffTree.fromFile(testfile, DiffTreeParseOptions.Default);

        Show.diff(d, "original").showAndAwait();
        new CutNonEditedSubtrees(true).transform(d);
        Show.diff(d, "cut").showAndAwait();
    }
}

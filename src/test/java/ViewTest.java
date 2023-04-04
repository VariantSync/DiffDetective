import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.experiments.views.ViewAnalysis;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.diff.view.ViewSource;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.query.FeatureQuery;
import org.variantsync.diffdetective.variation.tree.view.query.Query;
import org.variantsync.diffdetective.variation.tree.view.query.TraceYesQuery;
import org.variantsync.diffdetective.variation.tree.view.query.VariantQuery;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.*;

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
                Show.baddiff(view, "view(tree(e), " + query.getName() + ")"),
                Show.diff(goodDiff, "unify(view(tree(e), " + query.getName() + "))")
        );
    }


    @ParameterizedTest
    @ValueSource(strings = {
//            "1",
            "runningexample",
//            "const",
//            "diamond",
//            "deep_insertion"
    })
    void test(String filename) throws IOException, DiffParseException {
        final Path testfile = resDir.resolve(filename + ".diff");
        Logger.debug("Testing " + testfile);

        // Load diff
        final DiffTree initialVDiff = DiffTree.fromFile(testfile, false, false);
        initialVDiff.assertConsistency();

//        showViews(initialVDiff, new VariantQuery(new And(new Literal("X"))));
//        showViews(initialVDiff, new VariantQuery(new And(new Literal("Y"))));
//        showViews(initialVDiff, new VariantQuery(new And(negate(new Literal("X")))));
//        showViews(initialVDiff, new VariantQuery(new And(negate(new Literal("Y")))));
        showViews(initialVDiff, new FeatureQuery("X"));
        showViews(initialVDiff, new FeatureQuery("Y"));
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

        final DiffTree d = DiffTree.fromFile(testfile, false, false);
        final VariationTree b = VariationTree.fromProjection(d.getRoot().projection(Time.BEFORE), VariationTreeSource.Unknown);
        final VariationTree a = VariationTree.fromProjection(d.getRoot().projection(Time.AFTER),  VariationTreeSource.Unknown);
        // Let's say Bob is expert in feature X only.
        final Query bobsQuery =
                new VariantQuery(and(negate(featureDoubleLink)));
//                new FeatureQuery(ring.toString());
        final Query charlottesQuery = new VariantQuery(and(
//                new Literal("X"),
                negate(featureRing)
        ));
//        GameEngine.showAndAwaitAll(
//                Show.diff(d, "D"),
////                Show.tree(b, "project(D, b)"),
////                Show.tree(a, "project(D, a)"),
//                Show.diff(DiffView.badgood(d, bobsQuery), "[BadGood] Bob's View: " + bobsQuery.getName()),
//                Show.diff(DiffView.optimized(d, bobsQuery), "[Optimized] Bob's View: " + bobsQuery.getName()),
//                Show.diff(DiffView.badgood(d, charlottesQuery), "[BadGood] Charlotte's View: " + charlottesQuery.getName()),
//                Show.diff(DiffView.optimized(d, charlottesQuery), "[Optimized] Charlotte's View: " + charlottesQuery.getName())
//        );

        final VariantQuery configureExample1 = new VariantQuery(
                and(featureRing, /* FM = */ new Implies(featureDoubleLink, negate(featureRing)))
        );
        final TraceYesQuery traceYesExample1 = new TraceYesQuery(
                featureDoubleLink
        );
        GameEngine.showAndAwaitAll(
                Show.tree(TreeView.tree(b, configureExample1), "view_{tree}(project_b(D), " + configureExample1.getName() + ")"),
                Show.tree(TreeView.tree(b, traceYesExample1), "view_{tree}(project_b(D), " + traceYesExample1.getName() + ")")
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

        final List<Node> configs = ViewAnalysis.getUniquePartialConfigs(d, false);
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
            Show.diff(view, i + ".) view(D, " + q.getName() + ")").showAndAwait();
        }
    }
}

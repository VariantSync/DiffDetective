import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.tree.view.View;
import org.variantsync.diffdetective.variation.tree.view.query.ArtifactQuery;
import org.variantsync.diffdetective.variation.tree.view.query.FeatureQuery;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

import java.io.IOException;
import java.nio.file.Path;

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
        View.inline(view.diff(), query);

        // unify
        final DiffTree goodDiff = view.toGood();
        goodDiff.assertConsistency();

        GameEngine.showAndAwaitAll(
                Show.diff(initialVDiff, "initial edit e"),
                Show.baddiff(badDiff, "tree(e)"),
                Show.baddiff(view, "view(tree(e), feature(B))"),
                Show.diff(goodDiff, "unify(view(tree(e), " + query.getName() + "))")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
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

        showViews(initialVDiff, new FeatureQuery("A"));
        showViews(initialVDiff, new FeatureQuery("B"));
        showViews(initialVDiff, new ArtifactQuery("y"));
    }
}

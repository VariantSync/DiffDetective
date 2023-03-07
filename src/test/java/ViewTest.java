import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.graph.FormalDiffGraph;
import org.variantsync.diffdetective.variation.tree.view.View;
import org.variantsync.diffdetective.variation.tree.view.query.FeatureQuery;

import java.io.IOException;
import java.nio.file.Path;

public class ViewTest {

    private static final Path resDir = Constants.RESOURCE_DIR.resolve("badvdiff");

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "const",
            "diamond",
            "deep_insertion"
    })
    void test(String filename) throws IOException, DiffParseException {
        final Path testfile = resDir.resolve(filename + ".diff");
        Logger.debug("Testing " + testfile);

        // Load diff
        final DiffTree initialVDiff = DiffTree.fromFile(testfile, false, false);
        initialVDiff.assertConsistency();
        Show.show(initialVDiff);

        // treeify
        final BadVDiff badDiff = BadVDiff.fromGood(initialVDiff);
        Show.show(badDiff);

        // create view
        View.inline(badDiff.diff(), new FeatureQuery("B"));
        Show.show(badDiff);

        // unify
        final DiffTree goodDiff = badDiff.toGood();
        goodDiff.assertConsistency();
        Show.show(badDiff);
    }
}

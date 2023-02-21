import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.graph.GraphView;

import java.io.IOException;
import java.nio.file.Path;

public class BadVDiffTest {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("bad");

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "const",
            "diamond",
            "deep_insertion"
    })
    public void toGood_after_fromGood_idempotency(String filename) throws IOException, DiffParseException {
        final Path testfile = resDir.resolve(filename + ".diff");
        Logger.debug("Testing " + testfile);

        final DiffTree initialVDiff = DiffTree.fromFile(testfile, false, false);
        final GraphView initialVDiffAsGraph = GraphView.fromDiffTree(initialVDiff);
        Logger.debug("Initial:" + StringUtils.LINEBREAK + initialVDiffAsGraph);
        initialVDiff.assertConsistency();

        final BadVDiff badDiff = BadVDiff.fromGood(initialVDiff);
        Logger.debug("Bad:" + StringUtils.LINEBREAK + badDiff.prettyPrint());

        final DiffTree goodDiff = badDiff.toGood();
        final GraphView goodDiffAsGraph = GraphView.fromDiffTree(goodDiff);
        Logger.debug("Good:" + StringUtils.LINEBREAK + goodDiffAsGraph);
        goodDiff.assertConsistency();

        Assertions.assertEquals(initialVDiffAsGraph, goodDiffAsGraph);
    }
}

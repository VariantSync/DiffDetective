import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;

import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;

import java.io.IOException;
import java.nio.file.Path;

public class ShowTest {
    @Disabled("GUI test, needs to be run manually")
    @Test
    public void toGood_after_fromGood_idempotency() throws IOException, DiffParseException {
        final Path testfile = Constants.RESOURCE_DIR.resolve("badvdiff").resolve("1.diff");
        final VariationDiff<DiffLinesLabel> vdiff = VariationDiff.fromFile(testfile, new VariationDiffParseOptions(false, false));
        final BadVDiff<DiffLinesLabel> badDiff = BadVDiff.fromGood(vdiff);

        GameEngine.showAndAwaitAll(Show.baddiff(badDiff));
    }
}

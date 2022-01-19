import de.variantsync.functjonal.Product;
import diff.difftree.DiffTree;
import diff.difftree.parse.DiffNodeParser;
import diff.difftree.parse.DiffTreeParser;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.DiffTreeSerializeDebugData;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.LineGraphExport;
import diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.junit.Assert;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import util.IO;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;

public class TestMultiLineMacros {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("multilinemacros");

    public void diffToDiffTree(DiffTreeLineGraphExportOptions exportOptions, Path p) throws IOException {
        final String fullDiff = IO.readAsString(p);

        final DiffTree tree = DiffTreeParser.createDiffTree(
                fullDiff,
                true,
                true,
                DiffNodeParser.Default).unwrap().getSuccess();

        final Product<DiffTreeSerializeDebugData, String> result = LineGraphExport.toLineGraphFormat(tree, exportOptions);
        Assert.assertNotNull(result);
        final DiffTreeSerializeDebugData debugData = result.first();
        Logger.info("Parsed " + debugData.numExportedNonNodes + " nodes of diff type NON.");
        Logger.info("Parsed " + debugData.numExportedAddNodes + " nodes of diff type ADD.");
        Logger.info("Parsed " + debugData.numExportedRemNodes + " nodes of diff type REM.");

        final String lg = "t # 1" +
                StringUtils.LINEBREAK +
                result.second();

        IO.write(resDir.resolve("gen").resolve(p.getFileName() + ".lg"), lg);
    }

    @Test
    public void test() throws IOException {
        final DiffTreeLineGraphExportOptions exportOptions = new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE,
                new CommitDiffDiffTreeLabelFormat(),
                new DebugDiffNodeFormat()
        );

        diffToDiffTree(exportOptions, resDir.resolve("mldiff1.txt"));
        diffToDiffTree(exportOptions, resDir.resolve("diffWithComments.txt"));
    }
}

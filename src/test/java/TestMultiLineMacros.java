import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.difftree.DiffTree;
import diff.difftree.parse.DiffTreeParser;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.nodeformat.MiningDiffNodeLineGraphImporter;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.serialize.DiffTreeSerializeDebugData;
import diff.serialize.LineGraphExport;
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
                true);

        final Pair<DiffTreeSerializeDebugData, String> result = LineGraphExport.toLineGraphFormat(tree, exportOptions);
        final DiffTreeSerializeDebugData debugData = result.getKey();
        Logger.info("Parsed " + debugData.numExportedNonNodes + " nodes of diff type NON.");
        Logger.info("Parsed " + debugData.numExportedAddNodes + " nodes of diff type ADD.");
        Logger.info("Parsed " + debugData.numExportedRemNodes + " nodes of diff type REM.");

        final String lg = "t # 1" +
                StringUtils.LINEBREAK +
                result.getValue();

        IO.write(resDir.resolve("gen").resolve(p.getFileName() + ".lg"), lg);
    }

    @Test
    public void test() throws IOException {
        final DiffTreeLineGraphExportOptions exportOptions = new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFGRAPH,
                new CommitDiffDiffTreeLabelFormat(),
                new MiningDiffNodeLineGraphImporter()
        );

        diffToDiffTree(exportOptions, resDir.resolve("mldiff1.txt"));
        diffToDiffTree(exportOptions, resDir.resolve("diffWithComments.txt"));
    }
}

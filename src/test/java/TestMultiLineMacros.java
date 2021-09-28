import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.difftree.DiffTree;
import diff.difftree.parse.DiffTreeParser;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import diff.serialize.DiffTreeSerializeDebugData;
import util.IO;
import diff.serialize.LineGraphExport;
import util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class TestMultiLineMacros {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("multilinemacros");

    public void diffToDiffTree(LineGraphExport.Options exportOptions, Path p) throws IOException {
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
        final LineGraphExport.Options exportOptions = new LineGraphExport.Options(
                LineGraphExport.NodePrintStyle.Verbose
        );

        diffToDiffTree(exportOptions, resDir.resolve("mldiff1.txt"));
        diffToDiffTree(exportOptions, resDir.resolve("diffWithComments.txt"));
    }
}

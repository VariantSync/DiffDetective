import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.data.DiffTree;
import diff.data.difftreeparse.DiffTreeParser;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import util.DebugData;
import util.ExportUtils;
import util.LineGraphExport;
import util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class TestMultiLineMacros {
    private static final Path resDir = Path.of("src", "test", "resources", "multilinemacros");

    public static String readAsString(final Path p) throws IOException {
        try (
                final FileReader f = new FileReader(p.toFile());
                final BufferedReader reader = new BufferedReader(f)
        ) {
            return reader.lines().collect(Collectors.joining("\r\n"));
        } catch (final IOException e) {
            Logger.error("Failed to read lines from file: ", e);
            throw e;
        }
    }

    public void diffToDiffTree(LineGraphExport.Options exportOptions, Path p) throws IOException {
        final String fullDiff = readAsString(p);

        final DiffTree tree = DiffTreeParser.createDiffTree(
                fullDiff,
                true,
                true);

        final Pair<DebugData, String> result = LineGraphExport.toLineGraphFormat(tree, exportOptions);
        final DebugData debugData = result.getKey();
        Logger.info("Parsed " + debugData.numExportedNonNodes + " nodes of diff type NON.");
        Logger.info("Parsed " + debugData.numExportedAddNodes + " nodes of diff type ADD.");
        Logger.info("Parsed " + debugData.numExportedRemNodes + " nodes of diff type REM.");

        final String lg = "t # 1" +
                StringUtils.LINEBREAK +
                result.getValue();

        ExportUtils.write(resDir.resolve("gen").resolve(p.getFileName() + ".lg"), lg);
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

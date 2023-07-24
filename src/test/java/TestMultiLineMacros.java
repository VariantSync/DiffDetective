import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.tinylog.Logger;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.variation.diff.serialize.VariationDiffSerializeDebugData;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExport;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DebugDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestMultiLineMacros {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("multilinemacros");

    public void diffToVariationDiff(LineGraphExportOptions<DiffLinesLabel> exportOptions, Path p) throws IOException, DiffParseException {
        VariationDiff<DiffLinesLabel> tree;
        try (BufferedReader fullDiff = Files.newBufferedReader(p)) {
            tree = VariationDiffParser.createVariationDiff(
                    fullDiff,
                    new VariationDiffParseOptions(
                            CPPAnnotationParser.Default,
                            true,
                            false
                    ));
        }

        try (var destination = IO.newBufferedOutputStream(resDir.resolve("gen").resolve(p.getFileName() + ".lg"))) {
            destination.write(("t # 1" + StringUtils.LINEBREAK).getBytes());

            final VariationDiffSerializeDebugData debugData = LineGraphExport.toLineGraphFormat(tree, exportOptions, destination);
            assertNotNull(debugData);
            Logger.info("Parsed {} nodes of diff type NON.", debugData.numExportedNonNodes);
            Logger.info("Parsed {} nodes of diff type ADD.", debugData.numExportedAddNodes);
            Logger.info("Parsed {} nodes of diff type REM.", debugData.numExportedRemNodes);

        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "mldiff1.txt", "diffWithComments.txt" })
    public void test(String filename) throws IOException, DiffParseException {
        final LineGraphExportOptions<DiffLinesLabel> exportOptions = new LineGraphExportOptions<>(
                GraphFormat.VARIATION_DIFF,
                new CommitDiffVariationDiffLabelFormat(),
                new DebugDiffNodeFormat<>(),
                new DefaultEdgeLabelFormat<>()
        );

        diffToVariationDiff(exportOptions, resDir.resolve(filename));
    }
}

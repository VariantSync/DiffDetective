import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExporter;
import org.variantsync.diffdetective.diff.difftree.serialize.Format;
import org.variantsync.diffdetective.diff.difftree.serialize.TikzExporter;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.FullNodeFormat;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DiffTreeParserTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");

    @Test
    public void test01() throws IOException, DiffParseException {
        testCase("01");
    }

    @Test
    public void test02() throws IOException, DiffParseException {
        testCase("02");
    }

    @Test
    public void test03() throws IOException, DiffParseException {
        testCase("03");
    }

    @Test
    public void test04() throws IOException, DiffParseException {
        testCase("04");
    }

    @Test
    public void test05() throws IOException, DiffParseException  {
        testCase("05");
    }

    @Test
    public void test06() throws IOException, DiffParseException  {
        testCase("06");
    }

    @Test
    public void test07() throws IOException, DiffParseException  {
        testCase("07");
    }

    @Test
    public void test08() throws IOException, DiffParseException  {
        testCase("08");
    }

    @Test
    public void test09() throws IOException, DiffParseException  {
        testCase("09");
    }

    @Test
    public void test10() throws IOException, DiffParseException  {
        testCase("10");
    }

    @Test
    public void test11() throws IOException, DiffParseException  {
        testCase("11");
    }

    @Test
    public void test12() throws IOException, DiffParseException  {
        testCase("12");
    }

    @Test
    public void test13() throws IOException, DiffParseException  {
        testCase("13");
    }

    @Test
    public void test14() throws IOException, DiffParseException  {
        testCase("14");
    }

    @Test
    public void test15() throws IOException, DiffParseException  {
        testCase("15");
    }

    @Test
    public void test16() throws IOException, DiffParseException  {
        testCase("16");
    }

    @Test
    public void test17() throws IOException, DiffParseException  {
        testCase("17");
    }

    @Test
    public void test18() throws IOException, DiffParseException  {
        testCase("18");
    }

    @Disabled("WONTFIX, would require comment parsing in DiffTreeParser")
    @Test
    public void test19() throws IOException, DiffParseException  {
        testCase("19");
    }

    @Disabled("WONTFIX, would require comment parsing in DiffTreeParser")
    @Test
    public void test20() throws IOException, DiffParseException  {
        testCase("20");
    }

    @Disabled("WONTFIX, would require comment parsing in DiffTreeParser")
    @Test
    public void test21() throws IOException, DiffParseException  {
        testCase("21");
    }

    @Disabled("WONTFIX, would require comment parsing in DiffTreeParser")
    @Test
    public void test22() throws IOException, DiffParseException  {
        testCase("22");
    }

    public void testCase(String basename) throws IOException, DiffParseException {
        var testCasePath = testDir.resolve(basename + ".diff");
        var actualPath = testDir.resolve(basename + "_actual.lg");
        var expectedPath = testDir.resolve(basename + "_expected.lg");

        DiffTree diffTree;
        try (var inputFile = Files.newBufferedReader(testCasePath)) {
            diffTree = DiffTreeParser.createDiffTree(
                inputFile,
                false,
                false,
                CPPAnnotationParser.Default
            );
        }

        try (
                var unbufferedOutput = Files.newOutputStream(actualPath);
                var output = new BufferedOutputStream(unbufferedOutput)
        ) {
            new LineGraphExporter(new Format(new FullNodeFormat(), new ChildOrderEdgeFormat()))
                .exportDiffTree(diffTree, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(expectedPath);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (!IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                var visualizationPath = testDir.resolve(basename + ".tex");
                new TikzExporter(new Format(new FullNodeFormat(), new DefaultEdgeLabelFormat()))
                    .exportFullLatexExample(diffTree, visualizationPath);
                fail("The DiffTree in file " + testCasePath + " didn't parse correctly. "
                    + "Expected the content of " + expectedPath + " but got the content of " + actualPath + ". "
                    + "Note: A visualisation is available at " + visualizationPath);
                // Keep output files if the test failed
            } else {
                // Delete output files if the test succeeded
                Files.delete(actualPath);
            }
        }
    }
}

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.error.UnparseableFormulaException;
import org.variantsync.diffdetective.feature.PreprocessorAnnotationParser;
import org.variantsync.diffdetective.feature.jpp.JPPDiffLineFormulaExtractor;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.FullNodeFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.variantsync.diffdetective.util.Assert.fail;

// Test cases for a parser of https://www.slashdev.ca/javapp/
public class JPPParserTest {
    private record TestCase<Input, Expected>(Input input, Expected expected) {
    }

    private record ThrowingTestCase(String formula) {
    }

    private static List<JPPParserTest.TestCase<String, String>> abstractionTests() {
        return List.of(
                /// #if expression
                // expression := <operand> <operator> <operand> | [!]defined(name)
                // expression := operand == operand
                new JPPParserTest.TestCase<>("//#if 1 == -42", "1__EQ____U_MINUS__42"),
                // expression := operand != operand
                new JPPParserTest.TestCase<>("// #if 1 != 0", "1__NEQ__0"),
                // expression := operand <= operand
                new JPPParserTest.TestCase<>("//#if -1 <= 0", "__U_MINUS__1__LEQ__0"),
                // expression := operand < operand
                new JPPParserTest.TestCase<>("//#if \"str\" < 0", "__QUOTE__str__QUOTE____LT__0"),
                // expression := operand >= operand
                new JPPParserTest.TestCase<>("//   #if \"str\" >= \"str\"", "__QUOTE__str__QUOTE____GEQ____QUOTE__str__QUOTE__"),
                // expression := operand > operand
                new JPPParserTest.TestCase<>("//  #if 1.2 > 0", "1__DOT__2__GT__0"),
                // expression := defined(name)
                new JPPParserTest.TestCase<>("//#if defined(property)", "DEFINED_property"),
                // expression := !defined(name)
                new JPPParserTest.TestCase<>("//#if !defined(property)", "__U_NOT__DEFINED_property"),
                // operand := ${property}
                new JPPParserTest.TestCase<>("//#if ${os_version} == 4.1", "os_version__EQ__4__DOT__1"),

                /// #if expression and expression
                new JPPParserTest.TestCase<>("//#if 1 > 2 and defined( FEAT_A  )", "1__GT__2&&DEFINED_FEAT_A"),

                /// #if expression or expression
                new JPPParserTest.TestCase<>("//#if !defined(left) or defined(right)", "__U_NOT__DEFINED_left||DEFINED_right"),

                /// #if expression and expression or expression
                new JPPParserTest.TestCase<>("//#if ${os_version} == 4.1 and 1 > -42 or defined(ALL)", "os_version__EQ__4__DOT__1&&1__GT____U_MINUS__42||DEFINED_ALL")
        );
    }

    private static List<JPPParserTest.ThrowingTestCase> throwingTestCases() {
        return List.of(
                // Invalid macro
                new JPPParserTest.ThrowingTestCase(""),
                new JPPParserTest.ThrowingTestCase("#"),
                new JPPParserTest.ThrowingTestCase("ifdef A"),
                new JPPParserTest.ThrowingTestCase("#error A"),
                new JPPParserTest.ThrowingTestCase("#iferror A"),

                // Empty formula
                new JPPParserTest.ThrowingTestCase("//#if"),
                new JPPParserTest.ThrowingTestCase("#if defined()"),
                new JPPParserTest.ThrowingTestCase("#if ${} > 0"),

                // incomplete expressions
                new JPPParserTest.ThrowingTestCase("#if 1 >"),
                new JPPParserTest.ThrowingTestCase("#if  == 2"),
                new JPPParserTest.ThrowingTestCase("#if  ${version} > ")
        );
    }

    private static List<JPPParserTest.TestCase<Path, Path>> fullDiffTests() {
        final Path basePath = Path.of("src", "test", "resources", "diffs", "jpp");
        return List.of(
                new JPPParserTest.TestCase<>(basePath.resolve("basic_jpp.diff"), basePath.resolve("basic_jpp_expected.lg"))
        );
    }

    @ParameterizedTest
    @MethodSource("abstractionTests")
    public void testCase(JPPParserTest.TestCase<String, String> testCase) throws UnparseableFormulaException {
        assertEquals(
                testCase.expected,
                new JPPDiffLineFormulaExtractor().extractFormula(testCase.input())
        );
    }

    @ParameterizedTest
    @MethodSource("throwingTestCases")
    public void throwingTestCase(JPPParserTest.ThrowingTestCase testCase) {
        assertThrows(UnparseableFormulaException.class, () ->
                new JPPDiffLineFormulaExtractor().extractFormula(testCase.formula)
        );
    }

    @ParameterizedTest
    @MethodSource("fullDiffTests")
    public void fullDiffTestCase(JPPParserTest.TestCase<Path, Path> testCase) throws IOException, DiffParseException {
        VariationDiff<DiffLinesLabel> variationDiff;
        try (var inputFile = Files.newBufferedReader(testCase.input)) {
            variationDiff = VariationDiffParser.createVariationDiff(
                    inputFile,
                    new VariationDiffParseOptions(
                            false,
                            false
                    ).withAnnotationParser(PreprocessorAnnotationParser.JPPAnnotationParser)
            );
        }

        Path actualPath = testCase.input.getParent().resolve(testCase.input.getFileName() + "_actual");
        try (var output = IO.newBufferedOutputStream(actualPath)) {
            new LineGraphExporter<>(new Format<>(new FullNodeFormat(), new ChildOrderEdgeFormat<>()))
                    .exportVariationDiff(variationDiff, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(testCase.expected);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                // Delete output files if the test succeeded
                Files.delete(actualPath);
            } else {
                // Keep output files if the test failed
                fail("The VariationDiff in file " + testCase.input + " didn't parse correctly. "
                        + "Expected the content of " + testCase.expected + " but got the content of " + actualPath + ". ");
            }
        }
    }

}

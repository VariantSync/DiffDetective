import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.error.UnparseableFormulaException;
import org.variantsync.diffdetective.feature.jpp.JPPDiffLineFormulaExtractor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @ParameterizedTest
    @MethodSource("abstractionTests")
    public void testCase(JPPParserTest.TestCase testCase) throws UnparseableFormulaException {
        assertEquals(
                testCase.expected,
                new JPPDiffLineFormulaExtractor().extractFormula((String) testCase.input())
        );
    }

    @ParameterizedTest
    @MethodSource("throwingTestCases")
    public void throwingTestCase(JPPParserTest.ThrowingTestCase testCase) {
        assertThrows(UnparseableFormulaException.class, () ->
                new JPPDiffLineFormulaExtractor().extractFormula(testCase.formula)
        );
    }

}

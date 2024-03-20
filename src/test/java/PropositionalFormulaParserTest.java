import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;

import java.util.List;

import static org.variantsync.diffdetective.util.Assert.assertEquals;

/**
 * Class containing tests of the parsing behaviour for the default implementation of PropositionalFormulaParser.
 * Goal: Special characters that occur in the output of a DiffLineFormulaExtractor must not confuse the parsing process of the PropositionalFormulaParser.
 * It is not designed to extensively test the functionality of the PropositionalFormulaParser itself as this is expected to be done by FeatureIDE already.
 *
 * @author Maximilian Glumann
 */
public class PropositionalFormulaParserTest {
    private record TestCase(String formula, Node expected) {
    }

    /**
     * These test cases are based on a subset of the CPPParserTest test cases.
     * It is not necessary to keep all test cases from CPPParserTest as most of them result in a single but long Literal anyway.
     */
    private static List<TestCase> testCases() {
        return List.of(
                new TestCase("A", new Literal("A")),
                new TestCase("!(A)", new Literal("A", false)),
                new TestCase("!A", new Literal("A", false)),

                new TestCase("A&&B", new And(new Literal("A"), new Literal("B"))),
                new TestCase("A||B", new Or(new Literal("A"), new Literal("B"))),
                new TestCase("A&&(B||C)", new And(new Literal("A"), new Or(new Literal("B"), new Literal("C")))),
                new TestCase("A&&B||C", new Or(new And(new Literal("A"), new Literal("B")), new Literal("C"))),

                new TestCase("A__GEQ__B&&C__GT__D", new And(new Literal("A__GEQ__B"), new Literal("C__GT__D"))),
                new TestCase("DEFINED___LB__A__RB__&&__LB__B__MUL__2__RB____GT__C", new And(new Literal("DEFINED___LB__A__RB__"), new Literal("__LB__B__MUL__2__RB____GT__C"))),
                new TestCase("(STDC__EQ__1)&&(DEFINED___LB__LARGE__RB__||DEFINED___LB__COMPACT__RB__)", new And(new Literal("STDC__EQ__1"), new Or(new Literal("DEFINED___LB__LARGE__RB__"), new Literal("DEFINED___LB__COMPACT__RB__")))),
                new TestCase("APR_CHARSET_EBCDIC&&!(__LB____SQUOTE__Z__SQUOTE____SUB____SQUOTE__A__SQUOTE____RB____EQ__25)", new And(new Literal("APR_CHARSET_EBCDIC"), new Literal("__LB____SQUOTE__Z__SQUOTE____SUB____SQUOTE__A__SQUOTE____RB____EQ__25", false))),
                new TestCase("A&&(B__GT__C)", new And(new Literal("A"), new Literal("B__GT__C"))),
                new TestCase("A&&(__LB__B__ADD__1__RB____GT____LB__C__L_OR__D__RB__)", new And(new Literal("A"), new Literal("__LB__B__ADD__1__RB____GT____LB__C__L_OR__D__RB__"))),
                new TestCase("DEFINED_HAS_ATTRIBUTE_&&HAS_ATTRIBUTE___LB__nonnull__RB__", new And(new Literal("DEFINED_HAS_ATTRIBUTE_"), new Literal("HAS_ATTRIBUTE___LB__nonnull__RB__"))),
                new TestCase("HAS_BUILTIN___LB__nonnull__RB__&&A", new And(new Literal("HAS_BUILTIN___LB__nonnull__RB__"), new Literal("A"))),
                new TestCase("A||(DEFINED___LB__NAME__RB__&&(NAME__GEQ__199630))", new Or(new Literal("A"), new And(new Literal("DEFINED___LB__NAME__RB__"), new Literal("NAME__GEQ__199630")))),
                new TestCase("(DEFINED___LB__NAME__RB__&&(NAME__GEQ__199905)&&(NAME__LT__1991011))||(NAME__GEQ__300000)||DEFINED___LB__NAME__RB__", new Or(new And(new Literal("DEFINED___LB__NAME__RB__"), new And(new Literal("NAME__GEQ__199905"), new Literal("NAME__LT__1991011"))), new Or(new Literal("NAME__GEQ__300000"), new Literal("DEFINED___LB__NAME__RB__")))),
                new TestCase("1__GT____U_MINUS__42", new Literal("1__GT____U_MINUS__42")),
                new TestCase("1__GT____U_PLUS__42", new Literal("1__GT____U_PLUS__42")),
                new TestCase("42__GT____U_TILDE__A", new Literal("42__GT____U_TILDE__A")),
                new TestCase("A__ADD__B__GT__42", new Literal("A__ADD__B__GT__42")),
                new TestCase("A__LSHIFT__B", new Literal("A__LSHIFT__B")),
                new TestCase("A__THEN__B__COLON__C", new Literal("A__THEN__B__COLON__C")),
                new TestCase("A__MUL____LB__B__ADD__C__RB__", new Literal("A__MUL____LB__B__ADD__C__RB__")),
                new TestCase("(__LB____SQUOTE__Z__SQUOTE____SUB____SQUOTE__A__SQUOTE____RB____EQ__25)", new Literal("__LB____SQUOTE__Z__SQUOTE____SUB____SQUOTE__A__SQUOTE____RB____EQ__25")),
                new TestCase("(__LB__GNUTLS_VERSION_MAJOR__ADD____LB__GNUTLS_VERSION_MINOR__GT__0__L_OR__GNUTLS_VERSION_PATCH__GEQ__20__RB____RB____GT__3)", new Literal("__LB__GNUTLS_VERSION_MAJOR__ADD____LB__GNUTLS_VERSION_MINOR__GT__0__L_OR__GNUTLS_VERSION_PATCH__GEQ__20__RB____RB____GT__3")),
                new TestCase("(__LB__A__L_AND__B__RB____GT__C)", new Literal("__LB__A__L_AND__B__RB____GT__C")),
                new TestCase("A__EQ__B", new Literal("A__EQ__B")),
                new TestCase("(DEFINED_A)", new Literal("DEFINED_A")),
                new TestCase("MACRO___LB__A__B__RB____EQ__1", new Literal("MACRO___LB__A__B__RB____EQ__1")),
                new TestCase("ifndef", new Literal("ifndef")),
                new TestCase("__HAS_WARNING___LB____QUOTE____SUB__Wa__SUB__warning__QUOTE_____foo__RB__", new Literal("__HAS_WARNING___LB____QUOTE____SUB__Wa__SUB__warning__QUOTE_____foo__RB__"))
        );
    }

    /**
     * Each test case compares the output of the default PropositionalFormularParser to the expected output.
     * This comparison is performed using the equivalence defined by org.prop4j.Node from FeatureIDE.
     * Therefore, nodes describing equivalent propositional formulas in different tree structures are not considered equal.
     * As long as FeatureIDE produces a deterministic and consistent tree structure in its output, these tests will succeed.
     * Because DiffDetective desires not only a correct but also a deterministic and consistent parser output,
     * it is intended that these tests also break, if FeatureIDE changes its parsing behaviour in the future.
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void testCase(TestCase testCase) {
        assertEquals(
                testCase.expected,
                PropositionalFormulaParser.Default.parse(testCase.formula)
        );
    }
}
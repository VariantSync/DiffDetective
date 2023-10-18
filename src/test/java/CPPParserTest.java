import org.variantsync.diffdetective.error.UnparseableFormulaException;
import org.variantsync.diffdetective.feature.CPPDiffLineFormulaExtractor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

public class CPPParserTest {
    private static record TestCase(String formula, String expected) {}
    private static record ThrowingTestCase(String formula) {}

    private static List<TestCase> testCases() {
        return List.of(
            new TestCase("#if A", "A"),
            new TestCase("#ifdef A", "A"),
            new TestCase("#ifndef A", "!(A)"),
            new TestCase("#elif A", "A"),

            new TestCase("#if !A", "!A"),
            new TestCase("#if A && B", "A&&B"),
            new TestCase("#if A || B", "A||B"),
            new TestCase("#if A && (B || C)", "A&&(B||C)"),
            new TestCase("#if A && B || C", "A&&B||C"),

            new TestCase("#if 1 > -42", "1__GT____U_MINUS__42"),
            new TestCase("#if 1 > +42", "1__GT____U_PLUS__42"),
            new TestCase("#if 42 > A", "42__GT__A"),
            new TestCase("#if 42 > ~A", "42__GT____U_TILDE__A"),
            new TestCase("#if A + B > 42", "A__ADD__B__GT__42"),
            new TestCase("#if A << B", "A__LSHIFT__B"),
            new TestCase("#if A ? B : C", "A__THEN__B__COLON__C"),
            new TestCase("#if A >= B && C > D", "A__GEQ__B&&C__GT__D"),
            new TestCase("#if A * (B + C)", "A__MUL____LB__B__ADD__C__RB__"),
            new TestCase("#if defined(A) && (B * 2) > C", "DEFINED___LB__A__RB__&&__LB__B__MUL__2__RB____GT__C"),
            new TestCase("#if(STDC == 1) && (defined(LARGE) || defined(COMPACT))", "(STDC__EQ__1)&&(DEFINED___LB__LARGE__RB__||DEFINED___LB__COMPACT__RB__)"),
            new TestCase("#if (('Z' - 'A') == 25)", "(__LB____SQUOTE__Z__SQUOTE____SUB____SQUOTE__A__SQUOTE____RB____EQ__25)"),
            new TestCase("#if APR_CHARSET_EBCDIC && !(('Z' - 'A') == 25)", "APR_CHARSET_EBCDIC&&!(__LB____SQUOTE__Z__SQUOTE____SUB____SQUOTE__A__SQUOTE____RB____EQ__25)"),
            new TestCase("# if ((GNUTLS_VERSION_MAJOR + (GNUTLS_VERSION_MINOR > 0 || GNUTLS_VERSION_PATCH >= 20)) > 3)",
                    "(__LB__GNUTLS_VERSION_MAJOR__ADD____LB__GNUTLS_VERSION_MINOR__GT__0__L_OR__GNUTLS_VERSION_PATCH__GEQ__20__RB____RB____GT__3)"),

            new TestCase("#if A && (B > C)", "A&&(B__GT__C)"),
            new TestCase("#if (A && B) > C", "__LB__A__L_AND__B__RB____GT__C"),
            new TestCase("#if C == (A || B)", "C__EQ____LB__A__L_OR__B__RB__"),
            new TestCase("#if ((A && B) > C)", "(__LB__A__L_AND__B__RB____GT__C)"),
            new TestCase("#if A && ((B + 1) > (C || D))", "A&&(__LB__B__ADD__1__RB____GT____LB__C__L_OR__D__RB__)"),

            new TestCase("#if __has_include", "HAS_INCLUDE_"),
            new TestCase("#if defined __has_include", "DEFINED_HAS_INCLUDE_"),
            new TestCase("#if __has_include(<nss3/nss.h>)", "HAS_INCLUDE___LB____LT__nss3__DIV__nss__DOT__h__GT____RB__"),
            new TestCase("#if __has_include(<nss.h>)", "HAS_INCLUDE___LB____LT__nss__DOT__h__GT____RB__"),
            new TestCase("#if __has_include(\"nss3/nss.h\")", "HAS_INCLUDE___LB____QUOTE__nss3__DIV__nss__DOT__h__QUOTE____RB__"),
            new TestCase("#if __has_include(\"nss.h\")", "HAS_INCLUDE___LB____QUOTE__nss__DOT__h__QUOTE____RB__"),

            new TestCase("#if __has_attribute", "HAS_ATTRIBUTE_"),
            new TestCase("#if defined __has_attribute", "DEFINED_HAS_ATTRIBUTE_"),
            new TestCase("#  if __has_attribute (nonnull)", "HAS_ATTRIBUTE___LB__nonnull__RB__"),
            new TestCase("#if defined __has_attribute && __has_attribute (nonnull)", "DEFINED_HAS_ATTRIBUTE_&&HAS_ATTRIBUTE___LB__nonnull__RB__"),

            new TestCase("#if __has_cpp_attribute", "HAS_CPP_ATTRIBUTE_"),
            new TestCase("#if defined __has_cpp_attribute", "DEFINED_HAS_CPP_ATTRIBUTE_"),
            new TestCase("#if __has_cpp_attribute (nonnull)", "HAS_CPP_ATTRIBUTE___LB__nonnull__RB__"),
            new TestCase("#if __has_cpp_attribute (nonnull) && A", "HAS_CPP_ATTRIBUTE___LB__nonnull__RB__&&A"),

            new TestCase("#if defined __has_c_attribute", "DEFINED_HAS_C_ATTRIBUTE_"),
            new TestCase("#if __has_c_attribute", "HAS_C_ATTRIBUTE_"),
            new TestCase("#if __has_c_attribute (nonnull)", "HAS_C_ATTRIBUTE___LB__nonnull__RB__"),
            new TestCase("#if __has_c_attribute (nonnull) && A", "HAS_C_ATTRIBUTE___LB__nonnull__RB__&&A"),

            new TestCase("#if defined __has_builtin", "DEFINED_HAS_BUILTIN_"),
            new TestCase("#if __has_builtin", "HAS_BUILTIN_"),
            new TestCase("#if __has_builtin (__nonnull)", "HAS_BUILTIN___LB____nonnull__RB__"),
            new TestCase("#if __has_builtin (nonnull) && A", "HAS_BUILTIN___LB__nonnull__RB__&&A"),

            new TestCase("#if A // Comment && B", "A"),
            new TestCase("#if A /* Comment */ && B", "A&&B"),
            new TestCase("#if A && B /* Multiline Comment", "A&&B"),

            new TestCase("#if A == B", "A__EQ__B"),
            new TestCase("#if A == 1", "A__EQ__1"),

            new TestCase("#if defined A", "DEFINED_A"),
            new TestCase("#if defined(A)", "DEFINED___LB__A__RB__"),
            new TestCase("#if defined (A)", "DEFINED___LB__A__RB__"),
            new TestCase("#if defined ( A )", "DEFINED___LB__A__RB__"),
            new TestCase("#if (defined A)", "(DEFINED_A)"),
            new TestCase("#if MACRO (A)", "MACRO___LB__A__RB__"),
            new TestCase("#if MACRO (A, B)", "MACRO___LB__A__B__RB__"),
            new TestCase("#if MACRO (A, B + C)", "MACRO___LB__A__B__ADD__C__RB__"),
            new TestCase("#if MACRO (A, B) == 1", "MACRO___LB__A__B__RB____EQ__1"),

            new TestCase("#if ifndef", "ifndef"),

            new TestCase("#if __has_include_next(<some-header.h>)", "__HAS_INCLUDE_NEXT___LB____LT__some__SUB__header__DOT__h__GT____RB__"),
            new TestCase("#if __is_target_arch(x86)", "__IS_TARGET_ARCH___LB__x86__RB__"),
            new TestCase("#if A || (defined(NAME) && (NAME >= 199630))", "A||(DEFINED___LB__NAME__RB__&&(NAME__GEQ__199630))"),
            new TestCase("#if MACRO(part:part)", "MACRO___LB__part__COLON__part__RB__"),
            new TestCase("#if MACRO(x=1)", "MACRO___LB__x__ASSIGN__1__RB__"),
            new TestCase("#if A = 3", "A__ASSIGN__3"),
            new TestCase("#if ' ' == 32", "__SQUOTE_____SQUOTE____EQ__32"),
            new TestCase("#if (NAME<<1) > (1<<BITS)", "__LB__NAME__LSHIFT__1__RB____GT____LB__1__LSHIFT__BITS__RB__"),
            new TestCase("#if #cpu(sparc)", "CPU___LB__sparc__RB__"),
            new TestCase("#ifdef \\U0001000", "__B_SLASH__U0001000"),
            new TestCase("#if (defined(NAME) && (NAME >= 199905) && (NAME < 1991011)) ||     (NAME >= 300000) || defined(NAME)", "(DEFINED___LB__NAME__RB__&&(NAME__GEQ__199905)&&(NAME__LT__1991011))||(NAME__GEQ__300000)||DEFINED___LB__NAME__RB__"),
            new TestCase("#if __has_warning(\"-Wa-warning\"_foo)",
                    "__HAS_WARNING___LB____QUOTE____SUB__Wa__SUB__warning__QUOTE_____foo__RB__")
        );
    }

    private static List<ThrowingTestCase> throwingTestCases() {
        return List.of(
            // Invalid macro
            new ThrowingTestCase(""),
            new ThrowingTestCase("#"),
            new ThrowingTestCase("ifdef A"),
            new ThrowingTestCase("#error A"),
            new ThrowingTestCase("#iferror A"),

            // Empty formula
            new ThrowingTestCase("#ifdef"),
            new ThrowingTestCase("#ifdef // Comment"),
            new ThrowingTestCase("#ifdef /* Comment */")
        );
    }

    private static List<TestCase> wontfixTestCases() {
        return List.of(
            new TestCase("#if A == '1'", "A__EQ____TICK__1__TICK__"),
            new TestCase("#if A && (B - (C || D))", "A&&(B__MINUS__LB__C__LOR__D__RB__)")
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testCase(TestCase testCase) throws UnparseableFormulaException {
        assertEquals(
            testCase.expected,
            new CPPDiffLineFormulaExtractor().extractFormula(testCase.formula())
        );
    }

    @ParameterizedTest
    @MethodSource("throwingTestCases")
    public void throwingTestCase(ThrowingTestCase testCase) {
        assertThrows(UnparseableFormulaException.class, () ->
            new CPPDiffLineFormulaExtractor().extractFormula(testCase.formula)
        );
    }

    @Disabled("WONTFIX")
    @ParameterizedTest
    @MethodSource("wontfixTestCases")
    public void wontfixTestCase(TestCase testCase) throws UnparseableFormulaException {
        assertEquals(
            testCase.expected,
            new CPPDiffLineFormulaExtractor().extractFormula(testCase.formula())
        );
    }

}

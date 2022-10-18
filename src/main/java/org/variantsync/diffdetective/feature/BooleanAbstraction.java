package org.variantsync.diffdetective.feature;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Boolean abstraction for conditions in preprocessor macros.
 * Boolean abstraction heuristically reduces expressions in
 * higher-order logic (e.g., including arithmetics of function calls)
 * to a propositional formula.
 * Non-boolean expressions are replaced by respectively named variables.
 * @author Paul Bittner
 */
public class BooleanAbstraction {
    private BooleanAbstraction(){}

    /** Abstraction value for equality checks <code>==</code>. */
    public static final String EQ =  "__EQ__";
    /** Abstraction value for inequality checks <code>!=</code>. */
    public static final String NEQ =  "__NEQ__";
    /** Abstraction value for greater-equals checks <code>&gt;=</code>. */
    public static final String GEQ = "__GEQ__";
    /** Abstraction value for smaller-equals checks <code>&lt;=</code>. */
    public static final String LEQ = "__LEQ__";
    /** Abstraction value for greater checks <code>&gt;</code>. */
    public static final String GT = "__GT__";
    /** Abstraction value for smaller checks <code>&lt;</code>. */
    public static final String LT = "__LT__";
    /** Abstraction value for subtractions <code>-</code>. */
    public static final String SUB = "__SUB__";
    /** Abstraction value for additions <code>+</code>. */
    public static final String ADD = "__ADD__";
    /** Abstraction value for multiplications <code>*</code>. */
    public static final String MUL = "__MUL__";
    /** Abstraction value for divisions <code>/</code>. */
    public static final String DIV = "__DIV__";
    /** Abstraction value for modulo <code>%</code>. */
    public static final String MOD = "__MOD__";
    /** Abstraction value for bitwise left shift <code>&lt;&lt;</code>. */
    public static final String LSHIFT = "__LSHIFT__";
    /** Abstraction value for bitwise right shift <code>&gt;&gt;</code>. */
    public static final String RSHIFT = "__RSHIFT__";
    /** Abstraction value for bitwise not <code>~</code>. */
    public static final String NOT = "__NOT__";
    /** Abstraction value for bitwise and <code>&amp;</code>. */
    public static final String AND = "__AND__";
    /** Abstraction value for bitwise or <code>|</code>. */
    public static final String OR = "__OR__";
    /** Abstraction value for bitwise xor <code>^</code>. */
    public static final String XOR = "__XOR__";
    /** Abstraction value for the condition of the ternary operator <code>?</code>. */
    public static final String THEN = "__THEN__";
    /** Abstraction value for the alternative of the ternary operator <code>:</code>. */
    public static final String ELSE = "__ELSE__";
    /** Abstraction value for opening brackets <code>(</code>. */
    public static final String BRACKET_L = "__LB__";
    /** Abstraction value for clsong brackets <code>)</code>. */
    public static final String BRACKET_R = "__RB__";

    private static class Replacement {
        private Pattern pattern;
        private String replacement;

        /**
         * @param original the literal string to be replaced if it matches a whole word
         * @param the literal replacement
         */
        public Replacement(String original, String replacement) {
            this.pattern = Pattern.compile("(?<=\\b|[()])" + Pattern.quote(original) + "(?=\\b|[()])");
            this.replacement = Matcher.quoteReplacement(replacement);
        }

        public String applyTo(String value) {
            return pattern.matcher(value).replaceAll(replacement);
        }
    }

    private static final List<Replacement> ARITHMETICS = List.of(
        new Replacement("==", EQ),
        new Replacement("!=", NEQ),
        new Replacement(">=", GEQ),
        new Replacement("<=", LEQ),
        new Replacement(">", GT),
        new Replacement("<", LT),
        new Replacement("+", ADD),
        new Replacement("-", SUB),
        new Replacement("*", MUL),
        new Replacement("/", DIV),
        new Replacement("%", MOD),
        new Replacement("<<", LSHIFT),
        new Replacement(">>", RSHIFT),
        new Replacement("~", NOT),
        new Replacement("&", AND),
        new Replacement("^", XOR),
        new Replacement("|", OR),
        new Replacement("?", THEN),
        new Replacement(":", ELSE)
    );

    private static final Pattern COMMA = Pattern.compile(",");
    private static final String COMMA_REPLACEMENT = "__";
    private static final Pattern CALL = Pattern.compile("\\((\\w*)\\)");
    private static final String CALL_REPLACEMENT = BRACKET_L + "$1" + BRACKET_R;

    private static String abstractAll(String formula, final List<Replacement> replacements) {
        for (var replacement : replacements) {
            formula = replacement.applyTo(formula);
        }
        return formula;
    }

    /**
     * Abstracts all arithmetics in the given formula.
     * For example, a formula "3 >= 1 + 2" would be abstracted to a single variable "3__GEQ__1__ADD__2".
     * The given formula should be a string of a CPP conforming condition.
     * @param formula The formula whose arithmetics should be abstracted.
     * @return A copy of the formula with abstracted arithmetics.
     */
    public static String arithmetics(final String formula) {
        return abstractAll(formula, ARITHMETICS);
    }

    /**
     * Abstracts parentheses, including the commas of macro calls, in the given formula.
     *
     * For example, a call "FOO(3, 4, lol)" would be abstracted to a single variable "FOO__3__4__lol".
     * The given formula should be a string of a CPP conforming condition.
     * @param formula The formula whose function calls should be abstracted.
     * @return A copy of the formula with abstracted function calls.
     */
    public static String parentheses(String formula) {
        ////// abstract function calls
        /// replace commata in macro calls
        formula = COMMA.matcher(formula).replaceAll(COMMA_REPLACEMENT);

        /// inline macro calls as long as there are some
        /// Example
        ///    bar(2, foo(A__MUL__(B__PLUS__C))
        /// -> bar(2__foo(A__MUL__(B__PLUS__C))) // because of the comma replacement above
        /// -> bar(2__foo(A__MUL____LB__B__PLUS__C__RB__))
        /// -> bar(2__foo__LB__A__MUL____LB__B__PLUS__C__RB____RB__)
        /// -> bar__LB__2__foo__LB__A__MUL____LB__B__PLUS__C__RB____RB____RB__
        String old;
        do {
            old = formula;
            formula = CALL.matcher(formula).replaceAll(CALL_REPLACEMENT);
//            formula = formula.replaceAll("(\\w+)\\((\\w*)\\)", "$1__$2");
        } while (!old.equals(formula));

        return formula;
    }
}

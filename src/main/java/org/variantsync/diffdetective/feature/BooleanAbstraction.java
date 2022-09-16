package org.variantsync.diffdetective.feature;

import org.variantsync.functjonal.Functjonal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
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
    /** Abstraction value for greater-equals checks <code>&gt;=</code>. */
    public static final String GEQ = "__GEQ__";
    /** Abstraction value for smaller-equals checks <code>&lt;=</code>. */
    public static final String LEQ = "__LEQ__";
    /** Abstraction value for greater checks <code>&gt;</code>. */
    public static final String GT = "__GT__";
    /** Abstraction value for smaller checks <code>&lt;</code>. */
    public static final String LT = "__LT__";
    /** Abstraction value for substractions <code>-</code>. */
    public static final String SUB = "__SUB__";
    /** Abstraction value for additions <code>+</code>. */
    public static final String ADD = "__ADD__";
    /** Abstraction value for multiplications <code>*</code>. */
    public static final String MUL = "__MUL__";
    /** Abstraction value for divisions <code>/</code>. */
    public static final String DIV = "__DIV__";
    /** Abstraction value for modulo <code>%</code>. */
    public static final String MOD = "__MOD__";
    /** Abstraction value for opening brackets <code>(</code>. */
    public static final String BRACKET_L = "__LB__";
    /** Abstraction value for clsong brackets <code>)</code>. */
    public static final String BRACKET_R = "__RB__";

    private static final Map<Pattern, String> ARITHMETICS;
    static {
        ARITHMETICS = compile(Map.of(
                "==", EQ,
                ">=", GEQ,
                "<=", LEQ,
                ">", GT,
                "<", LT,
                Pattern.quote("+"), ADD,
                "-", SUB,
                Pattern.quote("*"), MUL,
                "/", DIV,
                "%", MOD
        ));
    }
    private static final Pattern COMMA = Pattern.compile(",");
    private static final String COMMA_REPLACEMENT = "__";

    private static final Pattern BRACKETS = Pattern.compile("\\((\\w*)\\)");
    private static final String BRACKETS_REPLACEMENT = BRACKET_L + "$1" + BRACKET_R;

    private static final Pattern CALL = Pattern.compile("(\\w+)\\((\\w*)\\)");
    private static final String CALL_REPLACEMENT = "$1__$2";

    private static Map<Pattern, String> compile(final Map<String, String> regex_replace) {
        return Functjonal.bimap(
                regex_replace,
                Pattern::compile,
                Function.identity(),
                // Use a linked hashmap here to ensure that regexes are always replaced in the same order.
                LinkedHashMap::new
        );
    }

    private static String abstractAll(String formula, final Map<Pattern, String> regex_replace) {
        for (Map.Entry<Pattern, String> regex : regex_replace.entrySet()) {
            formula = regex.getKey().matcher(formula).replaceAll(regex.getValue());
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
        // TODO: The extra call for BRACKETS could be avoided by adding it to the ARITHMETICS map.
        //       This requires a sorted map (e.g., LinkedHashMap) though which is not yet supported by the smart
        //       constructor Map.of.
        return BRACKETS.matcher(abstractAll(formula, ARITHMETICS)).replaceAll(BRACKETS_REPLACEMENT);
    }

    /**
     * Abstracts all function calls in the given formula.
     * For example, a call "FOO(3, 4, lol)" would be abstracted to a single variable "FOO__3__4__lol".
     * The given formula should be a string of a CPP conforming condition.
     * @param formula The formula whose function calls should be abstracted.
     * @return A copy of the formula with abstracted function calls.
     */
    public static String functionCalls(String formula) {
        ////// abstract function calls
        /// replace commata in macro calls
        formula = COMMA.matcher(formula).replaceAll(COMMA_REPLACEMENT);

        /// inline macro calls as long as there are some
        /// Example
        ///    bar(2, foo(baz))
        /// -> bar(2__foo(baz)) // because of the comma replacement above
        /// -> bar(2__foo__baz)
        /// -> bar__2__foo__baz
        String old;
        do {
            old = formula;
            formula = CALL.matcher(formula).replaceAll(CALL_REPLACEMENT);
//            formula = formula.replaceAll("(\\w+)\\((\\w*)\\)", "$1__$2");
        } while (!old.equals(formula));

        return formula;
    }
}

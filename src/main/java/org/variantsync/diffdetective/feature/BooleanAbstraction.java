package org.variantsync.diffdetective.feature;

import org.variantsync.functjonal.Functjonal;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class BooleanAbstraction {
    private BooleanAbstraction(){}

    public static final String EQ =  "__EQ__";
    public static final String GEQ = "__GEQ__";
    public static final String LEQ = "__LEQ__";
    public static final String GT = "__GT__";
    public static final String LT = "__LT__";
    public static final String SUB = "__SUB__";
    public static final String ADD = "__ADD__";
    public static final String MUL = "__MUL__";
    public static final String DIV = "__DIV__";
    public static final String MOD = "__MOD__";

    private static final Map<Pattern, String> ARITHMETICS;
    static {
        ARITHMETICS = compile(Map.of(
                "==", EQ,
                ">=", GEQ,
                "<=", LEQ,
                ">", GT,
                "<", LT,
                "\\+", ADD,
                "-", SUB,
                "\\*", MUL,
                "/", DIV,
                "%", MOD
        ));
    }
    private static final Pattern COMMA = Pattern.compile(",");
    private static final String COMMA_REPLACEMENT = "__";
    private static final Pattern CALL = Pattern.compile("(\\w+)\\((\\w*)\\)");
    private static final String CALL_REPLACEMENT = "$1__$2";

    private static Map<Pattern, String> compile(final Map<String, String> regex_replace) {
        return Functjonal.bimap(
                regex_replace,
                Pattern::compile,
                Function.identity()
        );
    }

    private static String abstractAll(String formula, final Map<Pattern, String> regex_replace) {
        for (Map.Entry<Pattern, String> regex : regex_replace.entrySet()) {
            formula = regex.getKey().matcher(formula).replaceAll(regex.getValue());
        }
        return formula;
    }

    public static String arithmetics(final String formula) {
        return abstractAll(formula, ARITHMETICS);
    }

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

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
    /** Abstraction value for the alternative of the ternary operator <code>:</code>, or just colons. */
    public static final String COLON = "__COLON__";
    /** Abstraction value for opening brackets <code>(</code>. */
    public static final String BRACKET_L = "__LB__";
    /** Abstraction value for closing brackets <code>)</code>. */
    public static final String BRACKET_R = "__RB__";
    /** Abstraction value for unary 'and' <code>&amp;</code>. */
    public static final String U_AND = "__U_AND__";
    /** Abstraction value for unary star <code>*</code>. */
    public static final String U_STAR = "__U_STAR__";
    /** Abstraction value for unary plus <code>+</code>. */
    public static final String U_PLUS = "__U_PLUS__";
    /** Abstraction value for unary minus <code>-</code>. */
    public static final String U_MINUS = "__U_MINUS__";
    /** Abstraction value for unary tilde <code>~</code>. */
    public static final String U_TILDE = "__U_TILDE__";
    /** Abstraction value for unary not <code>!</code>. */
    public static final String U_NOT = "__U_NOT__";
    /** Abstraction value for logical and <code>&amp;&amp;</code>. */
    public static final String L_AND = "__L_AND__";
    /** Abstraction value for logical or <code>||</code>. */
    public static final String L_OR = "__L_OR__";
    /** Abstraction value for dots in paths <code>.</code>. */
    public static final String DOT = "__DOT__";
    /** Abstraction value for quotation marks in paths <code>"</code>. */
    public static final String QUOTE = "__QUOTE__";
    /** Abstraction value for single quotation marks <code>'</code>. */
    public static final String SQUOTE = "__SQUOTE__";
    /** Abstraction value for has_attribute operator <code>__has_attribute(ATTRIBUTE)</code>. */
    public static final String HAS_ATTRIBUTE = "HAS_ATTRIBUTE_";
    /** Abstraction value for has_cpp_attribute operator <code>__has_cpp_attribute(ATTRIBUTE)</code>. */
    public static final String HAS_CPP_ATTRIBUTE = "HAS_CPP_ATTRIBUTE_";
    /** Abstraction value for has_c_attribute operator <code>__has_c_attribute(ATTRIBUTE)</code>. */
    public static final String HAS_C_ATTRIBUTE = "HAS_C_ATTRIBUTE_";
    /** Abstraction value for has_builtin operator <code>__has_builtin(BUILTIN)</code>. */
    public static final String HAS_BUILTIN = "HAS_BUILTIN_";
    /** Abstraction value for has_include operator <code>__has_include(INCLUDE)</code>. */
    public static final String HAS_INCLUDE = "HAS_INCLUDE_";
    /** Abstraction value for defined operator <code>defined</code>. */
    public static final String DEFINED = "DEFINED_";
    /** Abstraction value for assign operator <code>=</code>. */
    public static final String ASSIGN = "__ASSIGN__";
    /** Abstraction value for star assign operator <code>*=</code>. */
    public static final String STAR_ASSIGN = "__STA___ASSIGN__";
    /** Abstraction value for div assign operator <code>/=</code>. */
    public static final String DIV_ASSIGN = "__DIV___ASSIGN__";
    /** Abstraction value for mod assign operator <code>%=</code>. */
    public static final String MOD_ASSIGN = "__MOD___ASSIGN__";
    /** Abstraction value for plus assign operator <code>+=</code>. */
    public static final String PLUS_ASSIGN = "__PLU___ASSIGN__";
    /** Abstraction value for minus assign operator <code>-=</code>. */
    public static final String MINUS_ASSIGN = "__MIN___ASSIGN__";
    /** Abstraction value for left shift assign operator <code>&lt;&lt;=</code>. */
    public static final String LEFT_SHIFT_ASSIGN = "__LSH___ASSIGN__";
    /** Abstraction value for right shift assign operator <code>&gt;&gt;=</code>. */
    public static final String RIGHT_SHIFT_ASSIGN = "__RSH___ASSIGN__";
    /** Abstraction value for 'and' assign operator <code>&amp;=</code>. */
    public static final String AND_ASSIGN = "__AND___ASSIGN__";
    /** Abstraction value for xor assign operator <code>^=</code>. */
    public static final String XOR_ASSIGN = "__XOR___ASSIGN__";
    /** Abstraction value for 'or' assign operator <code>|=</code>. */
    public static final String OR_ASSIGN = "__OR___ASSIGN__";
    /** Abstraction value for whitespace <code> </code>. */
    public static final String WHITESPACE = "_";
    /** Abstraction value for backslash <code>\</code>. */
    public static final String BSLASH = "__B_SLASH__";

    private record Replacement(Pattern pattern, String replacement) {
        /**
         * @param pattern     the literal string to be replaced if it matches a whole word
         * @param replacement the replacement with special escape codes according to
         *                    {@link Matcher#replaceAll}
         */
        private Replacement {
        }

            /**
             * Creates a new replacement matching {@code original} literally.
             *
             * @param original    a string which is searched for literally (without any special
             *                    characters)
             * @param replacement the literal replacement for strings matched by {@code original}
             */
            public static Replacement literal(String original, String replacement) {
                return new Replacement(
                        Pattern.compile(Pattern.quote(original)),
                        Matcher.quoteReplacement(replacement)
                );
            }

            /**
             * Creates a new replacement matching {@code original} literally but only on word
             * boundaries.
             * <p>
             * A word boundary is defined as the transition from a word character (alphanumerical
             * characters) to a non-word character (everything else) or the transition from any
             * character to a bracket (the characters {@code (} and {@code )}).
             *
             * @param original    a string which is searched for as a whole word literally (without any
             *                    special characters)
             * @param replacement the literal replacement for strings matched by {@code original}
             */
            public static Replacement onlyFullWord(String original, String replacement) {
                return new Replacement(
                        Pattern.compile("(?<=\\b|[()])" + Pattern.quote(original) + "(?=\\b|[()])"),
                        Matcher.quoteReplacement(replacement)
                );
            }

            /**
             * Replaces all patterns found in {@code value} by its replacement.
             */
            public String applyTo(String value) {
                return pattern.matcher(value).replaceAll(replacement);
            }
        }

    private static final List<Replacement> REPLACEMENTS = List.of(
        // These replacements are carefully ordered by their length (longest first) to ensure that
        // the longest match is replaced first.
        Replacement.literal("<<", LSHIFT),
        Replacement.literal(">>", RSHIFT),
        Replacement.literal("==", EQ),
        Replacement.literal("!=", NEQ),
        Replacement.literal(">=", GEQ),
        Replacement.literal("<=", LEQ),
        Replacement.literal(">", GT),
        Replacement.literal("<", LT),
        Replacement.literal("+", ADD),
        Replacement.literal("-", SUB),
        Replacement.literal("*", MUL),
        Replacement.literal("/", DIV),
        Replacement.literal("%", MOD),
        Replacement.literal("^", XOR),
        Replacement.literal("~", NOT),
        Replacement.literal("?", THEN),
        Replacement.literal(":", COLON),
        Replacement.literal( "&&", L_AND), 
        Replacement.literal( "||", L_OR), 
        Replacement.literal( ".", DOT), 
        Replacement.literal( "\"", QUOTE), 
        Replacement.literal( "'", SQUOTE),
        Replacement.literal( "(", BRACKET_L),
        Replacement.literal( ")", BRACKET_R), 
        Replacement.literal( "__has_attribute", HAS_ATTRIBUTE), 
        Replacement.literal( "__has_cpp_attribute", HAS_CPP_ATTRIBUTE), 
        Replacement.literal( "__has_c_attribute", HAS_C_ATTRIBUTE), 
        Replacement.literal( "__has_builtin", HAS_BUILTIN), 
        Replacement.literal( "__has_include", HAS_INCLUDE), 
        Replacement.literal( "defined", DEFINED),
        Replacement.literal( "=", ASSIGN),
        Replacement.literal( "*=", STAR_ASSIGN),
        Replacement.literal( "/=", DIV_ASSIGN),
        Replacement.literal( "%=", MOD_ASSIGN),
        Replacement.literal( "+=", PLUS_ASSIGN),
        Replacement.literal( "-=", MINUS_ASSIGN),
        Replacement.literal( "<<=", LEFT_SHIFT_ASSIGN),
        Replacement.literal( ">>=", RIGHT_SHIFT_ASSIGN),
        Replacement.literal( "&=", AND_ASSIGN),
        Replacement.literal( "^=", XOR_ASSIGN),
        Replacement.literal( "|=", OR_ASSIGN),
        Replacement.literal( "\\", BSLASH),
        new Replacement( Pattern.compile("\\s+"), WHITESPACE),
        Replacement.onlyFullWord("&", AND), // && has to be left untouched
        Replacement.onlyFullWord("|", OR) // || has to be left untouched
    );

    /**
     * Apply all possible abstraction replacements for substrings of the given formula.
     * @param formula the formula to abstract
     * @return a fully abstracted formula
     */
    public static String abstractAll(String formula) {
        for (var replacement : BooleanAbstraction.REPLACEMENTS) {
            formula = replacement.applyTo(formula);
        }
        return formula;
    }

    /**
     * <p>
     * Search for the first replacement that matches the entire text and apply it. This is the case, if the given text
     * corresponds to a single token (e.g., '&amp;&amp;', '||'). If no replacement for the entire text is found (e.g., if the token
     * has no replacement), all possible replacements are applied to abstract substrings of the token that require
     * abstraction.
     * </p>
     *
     * <p>The purpose of this method is to achieve a slight speedup for scenarios in which the text usually contains a single
     * token. For example, this is useful when abstracting individual tokens of an extracted preprocessor formula
     * in {@link AbstractingCExpressionVisitor}. In all other cases, directly calling {@link #abstractAll(String)} should
     * be preferred.
     * </p>
     *
     * @param text the text to abstract
     * @return a fully abstracted text
     */
    public static String abstractToken(String text) {
        for (Replacement replacement : REPLACEMENTS) {
            if (replacement.pattern.matcher(text).matches()) {
                return replacement.applyTo(text);
            }
        }
        return abstractAll(text);
    }
}

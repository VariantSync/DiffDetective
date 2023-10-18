package org.variantsync.diffdetective.feature;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.tinylog.Logger;
import org.variantsync.diffdetective.error.UnparseableFormulaException;
import org.variantsync.diffdetective.error.UncheckedUnParseableFormulaException;
import org.variantsync.diffdetective.feature.antlr.CExpressionLexer;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;

import java.util.BitSet;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the expression from a C preprocessor statement.
 * For example, given the annotation "#if defined(A) || B()", the extractor would extract
 * "A || B". The extractor detects if, ifdef, ifndef and elif annotations.
 * (Other annotations do not have expressions.)
 * The given pre-processor statement might also a line in a diff (i.e., preceeded by a - or +).
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr
 */
public class CPPDiffLineFormulaExtractor {
    // ^[+-]?\s*#\s*(if|ifdef|ifndef|elif)(\s+(.*)|\((.*)\))$
    private static final String CPP_ANNOTATION_REGEX = "^[+-]?\\s*#\\s*(if|ifdef|ifndef|elif)(\\s+(.*)|(\\(.*\\)))$";
    private static final Pattern CPP_ANNOTATION_REGEX_PATTERN = Pattern.compile(CPP_ANNOTATION_REGEX);

    /**
     * Resolves any macros in the given formula that are relevant for feature annotations.
     * For example, in {@link org.variantsync.diffdetective.datasets.predefined.MarlinCPPDiffLineFormulaExtractor Marlin},
     * feature annotations are given by the custom <code>ENABLED</code> and <code>DISABLED</code> macros,
     * which have to be unwrapped.
     * @param formula The formula whose feature macros to resolve.
     * @return The parseable formula as string. The default implementation returns the input string.
     */
    protected String resolveFeatureMacroFunctions(String formula) {
        return formula;
    }

    /**
     * Extracts the feature formula as a string from a macro line (possibly within a diff).
     * @param line The line of which to get the feature mapping
     * @return The feature mapping as a String of the given line
     */
    public String extractFormula(final String line) throws UnparseableFormulaException {
        final Matcher matcher = CPP_ANNOTATION_REGEX_PATTERN.matcher(line);
        final Supplier<UnparseableFormulaException> couldNotExtractFormula = () ->
               new UnparseableFormulaException("Could not extract formula from line \""+ line + "\".");

        // Retrieve the formula from the macro line
        String fm;
        if (matcher.find()) {
            if (matcher.group(3) != null) {
                fm = matcher.group(3);
            } else {
                fm = matcher.group(4);
            }
        } else {
            throw couldNotExtractFormula.get();
        }

        // abstract complex formulas (e.g., if they contain arithmetics or macro calls)
        try {
            fm = abstractFormula(fm);
        } catch (UncheckedUnParseableFormulaException e) {
            throw e.inner();
        } catch (Exception e) {
            Logger.warn(e);
            throw new UnparseableFormulaException(e);
        }

        if (fm.isEmpty()) {
            throw couldNotExtractFormula.get();
        }

        // negate for ifndef
        if ("ifndef".equals(matcher.group(1))) {
            fm = "!(" + fm + ")";
        }

        return fm;
    }

    /**
     * Abstract the given formula.
     * <p>
     * First, the visitor uses ANTLR to parse the formula into a parse tree gives the tree to a {@link ControllingCExpressionVisitor}.
     * The visitor traverses the tree starting from the root, searching for subtrees that must be abstracted.
     * If such a subtree is found, the visitor calls an {@link AbstractingCExpressionVisitor} to abstract the part of
     * the formula in the subtree.
     *  </p>
     * @param formula that is to be abstracted
     * @return the abstracted formula
     */
    private String abstractFormula(String formula) {
        CExpressionLexer lexer = new CExpressionLexer(CharStreams.fromString(formula));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CExpressionParser parser = new CExpressionParser(tokens);
        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
                Logger.warn("syntax error: {} ; {}", s, e);
                Logger.warn("formula: {}", formula);
                throw new UncheckedUnParseableFormulaException(s, e);
            }

            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
            }
        });
        ParseTree tree = parser.expression();
        return tree.accept(new ControllingCExpressionVisitor()).toString();
    }
}

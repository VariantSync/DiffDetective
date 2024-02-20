package org.variantsync.diffdetective.feature.cpp;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.variantsync.diffdetective.error.UnparseableFormulaException;
import org.variantsync.diffdetective.feature.AbstractingFormulaExtractor;
import org.variantsync.diffdetective.feature.ParseErrorListener;
import org.variantsync.diffdetective.feature.antlr.CExpressionLexer;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the expression from a C preprocessor statement.
 * For example, given the annotation "#if defined(A) || B()", the extractor would extract
 * "A || B". The extractor detects if, ifdef, ifndef and elif annotations.
 * (Other annotations do not have expressions.)
 * The given pre-processor statement might also a line in a diff (i.e., preceeded by a - or +).
 *
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr, Alexander Schultheiß
 */
public class CPPDiffLineFormulaExtractor extends AbstractingFormulaExtractor {
    // ^[+-]?\s*#\s*(if|ifdef|ifndef|elif)(\s+(.*)|\((.*)\))$
    private static final String CPP_ANNOTATION_REGEX = "^[+-]?\\s*#\\s*(if|ifdef|ifndef|elif)(\\s+(.*)|(\\(.*\\)))$";
    private static final Pattern CPP_ANNOTATION_PATTERN = Pattern.compile(CPP_ANNOTATION_REGEX);

    public CPPDiffLineFormulaExtractor() {
        super(CPP_ANNOTATION_PATTERN);
    }

    /**
     * Extracts the feature formula as a string from a macro line (possibly within a diff).
     *
     * @param line The line of which to get the feature mapping
     * @return The feature mapping as a String of the given line
     */
    @Override
    public String extractFormula(final String line) throws UnparseableFormulaException {
        // Delegate the formula extraction to AbstractingFormulaExtractor
        String fm = super.extractFormula(line);

        // negate for ifndef
        final Matcher matcher = CPP_ANNOTATION_PATTERN.matcher(line);
        if (matcher.find() && "ifndef".equals(matcher.group(1))) {
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
     * </p>
     *
     * @param formula that is to be abstracted
     * @return the abstracted formula
     */
    @Override
    protected String abstractFormula(String formula) {
        CExpressionLexer lexer = new CExpressionLexer(CharStreams.fromString(formula));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CExpressionParser parser = new CExpressionParser(tokens);
        parser.addErrorListener(new ParseErrorListener(formula));

        return parser.expression().accept(new ControllingCExpressionVisitor()).toString();
    }
}

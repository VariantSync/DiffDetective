package org.variantsync.diffdetective.feature.jpp;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.variantsync.diffdetective.feature.AbstractingFormulaExtractor;
import org.variantsync.diffdetective.feature.ParseErrorListener;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionLexer;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionParser;

import java.util.regex.Pattern;

/**
 * Extracts the expression from a <a href="https://www.slashdev.ca/javapp/">JavaPP (Java PreProcessor)</a> statement .
 * For example, given the annotation "//#if defined(A) || B()", the extractor would extract "A || B".
 * The extractor detects if and elif annotations (other annotations do not have expressions).
 * The given JPP statement might also be a line in a diff (i.e., preceeded by a - or +).
 *
 * @author Alexander Schultheiß
 */
public class JPPDiffLineFormulaExtractor extends AbstractingFormulaExtractor {
    private static final String JPP_ANNOTATION_REGEX = "^[+-]?\\s*//\\s*#\\s*(if|elif)(\\s+(.*)|(\\(.*\\)))$";
    private static final Pattern JPP_ANNOTATION_PATTERN = Pattern.compile(JPP_ANNOTATION_REGEX);

    public JPPDiffLineFormulaExtractor() {
        super(JPP_ANNOTATION_PATTERN);
    }

    /**
     * Abstract the given formula.
     * <p>
     * First, the visitor uses ANTLR to parse the formula into a parse tree gives the tree to a {@link ControllingJPPExpressionVisitor}.
     * The visitor traverses the tree starting from the root, searching for subtrees that must be abstracted.
     * If such a subtree is found, the visitor calls an {@link AbstractingJPPExpressionVisitor} to abstract the part of
     * the formula in the subtree.
     * </p>
     *
     * @param formula that is to be abstracted
     * @return the abstracted formula
     */
    @Override
    protected String abstractFormula(String formula) {
        JPPExpressionLexer lexer = new JPPExpressionLexer(CharStreams.fromString(formula));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JPPExpressionParser parser = new JPPExpressionParser(tokens);
        parser.addErrorListener(new ParseErrorListener(formula));
        ParseTree tree = parser.expression();
        return tree.accept(new ControllingJPPExpressionVisitor()).toString();
    }
}

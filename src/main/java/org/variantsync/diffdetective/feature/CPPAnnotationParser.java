package org.variantsync.diffdetective.feature;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.error.UnParseableFormulaException;

/**
 * A parser of C-preprocessor annotations.
 * @author Paul Bittner
 */
public class CPPAnnotationParser {
    /**
     * Default CPPAnnotationParser. Created by invoking {@link #CPPAnnotationParser()}.
     */
    public static final CPPAnnotationParser Default = new CPPAnnotationParser();

    private final PropositionalFormulaParser formulaParser;
    private final CPPDiffLineFormulaExtractor extractor;

    /**
     * Invokes {@link #CPPAnnotationParser(PropositionalFormulaParser, CPPDiffLineFormulaExtractor)} with
     * the {@link PropositionalFormulaParser#Default default formula parser} and a new {@link CPPDiffLineFormulaExtractor}.
     */
    public CPPAnnotationParser() {
        this(PropositionalFormulaParser.Default, new CPPDiffLineFormulaExtractor());
    }

    /**
     * Creates a new preprocessor annotation parser.
     * @param formulaParser Parser that is used to parse propositional formulas in conditional annotations (e.g., the formula <code>f</code> in <code>#if f</code>).
     * @param extractor An extractor that extracts the formula part of a preprocessor annotation that is then given to the formulaParser.
     */
    public CPPAnnotationParser(final PropositionalFormulaParser formulaParser, CPPDiffLineFormulaExtractor extractor) {
        this.formulaParser = formulaParser;
        this.extractor = extractor;
    }

    /**
     * Parses the condition of the given line of source code that contains a preprocessor macro (i.e., IF, IFDEF, ELIF).
     * @param line The line of code of a preprocessor annotation.
     * @return The formula of the macro in the given line.
     *         If no such formula could be parsed, returns a Literal with the line's condition as name.
     * @throws UnParseableFormulaException when {@link CPPDiffLineFormulaExtractor#extractFormula(String)} throws.
     */
    public Node parseDiffLine(String line) throws UnParseableFormulaException {
        return parseCondition(extractor.extractFormula(line));
    }

    /**
     * Parses a condition of a preprocessor macro (i.e., IF, IFDEF, ELIF).
     * The given input should not start with preprocessor annotations.
     * If the input starts with a preprocessor annotation, use {@link #parseDiffLine} instead.
     * The input should have been prepared by {@link CPPDiffLineFormulaExtractor}.
     * @param condition The condition of a preprocessor annotation.
     * @return The formula of the condition.
     *         If no such formula could be parsed, returns a Literal with the condition as name.
     */
    public Node parseCondition(String condition) {
        Node formula = formulaParser.parse(condition);

        if (formula == null) {
//            Logger.warn("Could not parse expression '{}' to feature mapping. Using it as literal.", fmString);
            formula = new Literal(condition);
        }

        return formula;
    }
}

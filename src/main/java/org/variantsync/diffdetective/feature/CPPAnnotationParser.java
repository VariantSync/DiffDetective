package org.variantsync.diffdetective.feature;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.difftree.parse.IllFormedAnnotationException;

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
     * @return The formula of the macro in the given line. If no such formula could be parsed, returns a Literal with the line as name.
     * @throws IllFormedAnnotationException when {@link CPPDiffLineFormulaExtractor#extractFormula(String)} throws.
     */
    public Node parseDiffLine(String line) throws IllFormedAnnotationException {
        final String formulaStr = extractor.extractFormula(line);
        Node formula = formulaParser.parse(formulaStr);

        if (formula == null) {
//            Logger.warn("Could not parse expression '{}' to feature mapping. Using it as literal.", fmString);
            formula = new Literal(line);
        }

        return formula;
    }
}

package org.variantsync.diffdetective.feature;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.difftree.parse.IllFormedAnnotationException;

public class CPPAnnotationParser {
    public static final CPPAnnotationParser Default = new CPPAnnotationParser();

    private final PropositionalFormulaParser formulaParser;
    private final CPPDiffLineFormulaExtractor extractor;

    public CPPAnnotationParser() {
        this(PropositionalFormulaParser.Default, new CPPDiffLineFormulaExtractor());
    }

    public CPPAnnotationParser(final PropositionalFormulaParser formulaParser, CPPDiffLineFormulaExtractor extractor) {
        this.formulaParser = formulaParser;
        this.extractor = extractor;
    }

    public Node parseDiffLine(String line) throws IllFormedAnnotationException {
        final String formulaStr = extractor.extractFormula(line);
        Node formula = formulaParser.parse(formulaStr);

        if (formula == null) {
//            Logger.warn("Could not parse expression \"{}\" to feature mapping. Using it as literal.", fmString);
            formula = new Literal(line);
        }

        return formula;
    }
}

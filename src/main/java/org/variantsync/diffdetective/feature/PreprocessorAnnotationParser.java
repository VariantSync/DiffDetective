package org.variantsync.diffdetective.feature;

import org.prop4j.Node;
import org.variantsync.diffdetective.error.UnparseableFormulaException;
import org.variantsync.diffdetective.feature.cpp.CPPDiffLineFormulaExtractor;
import org.variantsync.diffdetective.feature.jpp.JPPDiffLineFormulaExtractor;

import java.util.regex.Pattern;

/**
 * A parser of preprocessor-like annotations.
 *
 * @author Paul Bittner, Alexander Schultheiß
 */
public class PreprocessorAnnotationParser implements AnnotationParser {
    /**
     * Matches the beginning or end of CPP conditional macros.
     * It doesn't match the whole macro name, for example for {@code #ifdef} only {@code "#if"} is
     * matched and only {@code "if"} is captured.
     * <p>
     * Note that this pattern doesn't handle comments between {@code #} and the macro name.
     */
    public final static Pattern CPP_PATTERN =
            Pattern.compile("^[+-]?\\s*#\\s*(if|elif|else|endif)");

    /**
     * Matches the beginning or end of JPP conditional macros.
     * It doesn't match the whole macro name, for example for {@code //#if defined(x)} only {@code "//#if"} is
     * matched and only {@code "if"} is captured.
     * <p>
     */
    public final static Pattern JPP_PATTERN =
            Pattern.compile("^[+-]?\\s*//\\s*#\\s*(if|elif|else|endif)");

    /**
     * Default parser for C preprocessor annotations.
     * Created by invoking {@link #PreprocessorAnnotationParser(Pattern, PropositionalFormulaParser, DiffLineFormulaExtractor)}.
     */
    public static final PreprocessorAnnotationParser CPPAnnotationParser =
            new PreprocessorAnnotationParser(CPP_PATTERN, PropositionalFormulaParser.Default, new CPPDiffLineFormulaExtractor());

    /**
     * Default parser for <a href="https://www.slashdev.ca/javapp/">JavaPP (Java PreProcessor)</a> annotations.
     * Created by invoking {@link #PreprocessorAnnotationParser(Pattern, PropositionalFormulaParser, DiffLineFormulaExtractor)}.
     */
    public static final PreprocessorAnnotationParser JPPAnnotationParser =
            new PreprocessorAnnotationParser(JPP_PATTERN, PropositionalFormulaParser.Default, new JPPDiffLineFormulaExtractor());

    // Pattern that is used to identify the AnnotationType of a given annotation.
    private final Pattern annotationPattern;
    private final PropositionalFormulaParser formulaParser;
    private final DiffLineFormulaExtractor extractor;

    /**
     * Invokes {@link #PreprocessorAnnotationParser(Pattern, PropositionalFormulaParser, DiffLineFormulaExtractor)} with
     * the {@link PropositionalFormulaParser#Default default formula parser} and a new {@link DiffLineFormulaExtractor}.
     *
     * @param annotationPattern Pattern that is used to identify the AnnotationType of a given annotation; {@link #CPP_PATTERN} provides an example
     */
    public PreprocessorAnnotationParser(final Pattern annotationPattern, final DiffLineFormulaExtractor formulaExtractor) {
        this(annotationPattern, PropositionalFormulaParser.Default, formulaExtractor);
    }

    /**
     * Creates a new preprocessor annotation parser.
     *
     * @param annotationPattern Pattern that is used to identify the AnnotationType of a given annotation; {@link #CPP_PATTERN} provides an example
     * @param formulaParser     Parser that is used to parse propositional formulas in conditional annotations (e.g., the formula <code>f</code> in <code>#if f</code>).
     * @param formulaExtractor  An extractor that extracts the formula part of a preprocessor annotation that is then given to the formulaParser.
     */
    public PreprocessorAnnotationParser(final Pattern annotationPattern, final PropositionalFormulaParser formulaParser, DiffLineFormulaExtractor formulaExtractor) {
        this.annotationPattern = annotationPattern;
        this.formulaParser = formulaParser;
        this.extractor = formulaExtractor;
    }

    /**
     * Parses the condition of the given line of source code that contains a preprocessor macro (i.e., IF, IFDEF, ELIF).
     *
     * @param line The line of code of a preprocessor annotation.
     * @return The formula of the macro in the given line.
     * If no such formula could be parsed, returns a Literal with the line's condition as name.
     * @throws UnparseableFormulaException when {@link DiffLineFormulaExtractor#extractFormula(String)} throws.
     */
    public Node parseAnnotation(String line) throws UnparseableFormulaException {
        return this.formulaParser.parse(extractor.extractFormula(line));
    }

    @Override
    public AnnotationType determineAnnotationType(String text) {
        var matcher = annotationPattern.matcher(text);
        int nameId = 1;
        if (matcher.find()) {
            return AnnotationType.fromName(matcher.group(nameId));
        } else {
            return AnnotationType.None;
        }
    }
}

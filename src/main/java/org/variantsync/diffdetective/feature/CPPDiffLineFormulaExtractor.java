package org.variantsync.diffdetective.feature;

import org.tinylog.Logger;
import org.variantsync.diffdetective.error.UnParseableFormulaException;
import org.variantsync.diffdetective.error.UncheckedUnParseableFormulaException;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the expression from a C preprocessor statement.
 * For example, given the annotation "#if defined(A) || B()", the extractor would extract
 * "A || B". The extractor detects if, ifdef, ifndef and elif annotations.
 * (Other annotations do not have expressions.)
 * The given pre processor statement might also a line in a diff (i.e., preceeded by a - or +).
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr
 */
public class CPPDiffLineFormulaExtractor {
    // ^[+-]?\s*#\s*(if|ifdef|ifndef|elif)(\s+(.*)|\((.*)\))$
    private static final String CPP_ANNOTATION_REGEX = "^[+-]?\\s*#\\s*(if|ifdef|ifndef|elif)(\\s+(.*)|(\\(.*\\)))$";
    private static final Pattern CPP_ANNOTATION_REGEX_PATTERN = Pattern.compile(CPP_ANNOTATION_REGEX);
    private static final Pattern COMMENT_PATTERN = Pattern.compile("(/\\*.*?\\*/)|(/\\*.*)");

    private static final ControllingCExpressionVisitor expressionSimplifier = new ControllingCExpressionVisitor();

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
    public String extractFormula(final String line) throws UnParseableFormulaException {
        // TODO: There still regexes here in replaceAll that could be optimized by precompiling the regexes once.
        final Matcher matcher = CPP_ANNOTATION_REGEX_PATTERN.matcher(line);
        final Supplier<UnParseableFormulaException> couldNotExtractFormula = () ->
               new UnParseableFormulaException("Could not extract formula from line \""+ line + "\".");

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

        // abstract arithmetics
        try {
            fm = expressionSimplifier.simplify(fm);
        } catch (UncheckedUnParseableFormulaException e) {
            throw e.inner();
        } catch (Exception e) {
            Logger.warn(e);
            throw new UnParseableFormulaException(e);
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
}

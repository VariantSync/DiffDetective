package org.variantsync.diffdetective.feature;

import org.variantsync.diffdetective.diff.difftree.parse.IllFormedAnnotationException;

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
    private static final String CPP_ANNOTATION_REGEX = "^[+-]?\\s*#\\s*(if|ifdef|ifndef|elif)(\\s+(.*)|\\((.*)\\))$";
    private static final Pattern CPP_ANNOTATION_REGEX_PATTERN = Pattern.compile(CPP_ANNOTATION_REGEX);
    private static final Pattern COMMENT_PATTERN = Pattern.compile("/\\*.*\\*/");
    private static final Pattern DEFINED_PATTERN = Pattern.compile("defined\\(([^)]*)\\)");

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
    public String extractFormula(final String line) throws IllFormedAnnotationException {
        // TODO: There still regexes here in replaceAll that could be optimized by precompiling the regexes once.
        final Matcher matcher = CPP_ANNOTATION_REGEX_PATTERN.matcher(line);

        String fm;
        if (matcher.find()) {
            if (matcher.group(3) != null) {
                fm = matcher.group(3);
            } else {
                fm = matcher.group(4);
            }
        } else {
            throw IllFormedAnnotationException.IfWithoutCondition("Could not extract formula from line \""+ line + "\".");
        }

        // remove comments
        fm = fm.split("//")[0];
        fm = COMMENT_PATTERN.matcher(fm).replaceAll("");

        // remove whitespace
        fm = fm.replaceAll("\\s", "");

        // remove defined()
        fm = DEFINED_PATTERN.matcher(fm).replaceAll("$1");
        fm = fm.replaceAll("defined ", " ");
        fm = resolveFeatureMacroFunctions(fm);

        ////// abstract arithmetics
        fm = BooleanAbstraction.arithmetics(fm);
        fm = BooleanAbstraction.functionCalls(fm);

        // negate for ifndef
        if (line.contains("ifndef")) {
            fm = "!(" + fm + ")";
        }

        return fm;
    }
}

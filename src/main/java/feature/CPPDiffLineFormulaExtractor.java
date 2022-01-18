package feature;

import diff.difftree.parse.IllFormedAnnotationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the expression from a C pre processor statement.
 * For example, given the annotation "#if defined(A) || B()", the extractor would extract
 * "A || B". The extractor detects if, ifdef, ifndef and elif annotations.
 * (Other annotations do not have expressions.)
 * The given pre processor statement might also a line in a diff (i.e., preceeded by a - or +).
 */
public class CPPDiffLineFormulaExtractor {
    // ^[+-]?\s*#\s*(if|ifdef|ifndef|elif)(\s+(.*)|\((.*)\))$
    public static final String CPP_ANNOTATION_REGEX = "^[+-]?\\s*#\\s*(if|ifdef|ifndef|elif)(\\s+(.*)|\\((.*)\\))$";
    public static final Pattern CPP_ANNOTATION_REGEX_PATTERN = Pattern.compile(CPP_ANNOTATION_REGEX);
    public static final String EQUAL_PLACEHOLDER = "__eq__";

    protected String resolveFeatureMacroFunctions(String formula) {
        return formula;
    }

    /**
     * Extracts the feature formula as a string from a macro line (possibly within a diff).
     * @param line The line of which to get the feature mapping
     * @return The feature mapping as a String of the given line
     */
    public String extractFormula(final String line) throws IllFormedAnnotationException {
        // TODO: There are so many regexes here in replaceAll that could be optimized by precompiling the regexes once.
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
        fm = fm.replaceAll("/\\*.*\\*/", "");

        // remove whitespace
        fm = fm.trim();

        // remove defined(), ENABLED() and DISABLED()
        fm = fm.replaceAll("defined\\s*\\(([^)]*)\\)", "$1");
        fm = fm.replaceAll("defined ", " ");
        fm = resolveFeatureMacroFunctions(fm);

        // remove whitespace
        fm = fm.replaceAll("\\s", "");

        // remove parentheses from custom cpp functions such as MB() or PIN_EXISTS()
        fm = fm.replaceAll("(\\w+)\\((\\w*)\\)", "$1__$2");

        // replace all "=="'s with a placeholder because NodeReader interprets these as propositional equality
        // but == does not denote propositional equality in macros
        fm = fm.replaceAll("==", EQUAL_PLACEHOLDER);

        // negate for ifndef
        if (line.contains("ifndef")) {
            fm = "!(" + fm + ")";
        }

        return fm;
    }
}

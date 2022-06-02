package org.variantsync.diffdetective.datasets.predefined;

import org.variantsync.diffdetective.feature.CPPDiffLineFormulaExtractor;

import java.util.regex.Pattern;

/**
 * Extracts formulas from preprocessor annotations in the marlin firmware.
 * In particular, it resolves the 'ENABLED' and 'DISABLED' macros that are used in Marlin
 * to check for features being (de-)selected.
 */
public class MarlinCPPDiffLineFormulaExtractor extends CPPDiffLineFormulaExtractor {
    private static Pattern ENABLED_PATTERN = Pattern.compile("ENABLED\\s*\\(([^)]*)\\)");
    private static Pattern DISABLED_PATTERN = Pattern.compile("DISABLED\\s*\\(([^)]*)\\)");

    @Override
    protected String resolveFeatureMacroFunctions(String formula) {
        return
            replaceAll(ENABLED_PATTERN, "$1",
                replaceAll(DISABLED_PATTERN, "!($1)",
                    super.resolveFeatureMacroFunctions(formula)));
    }

    private String replaceAll(Pattern pattern, String replacement, String string) {
        return pattern.matcher(string).replaceAll(replacement);
    }
}

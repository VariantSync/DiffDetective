package org.variantsync.diffdetective.datasets.predefined;

import org.variantsync.diffdetective.feature.CPPDiffLineFormulaExtractor;

/**
 * Extracts formulas from preprocessor annotations in the marlin firmware.
 * In particular, it resolves the 'ENABLED' and 'DISABLED' macros that are used in Marlin
 * to check for features being (de-)selected.
 */
public class MarlinCPPDiffLineFormulaExtractor extends CPPDiffLineFormulaExtractor {
    @Override
    protected String resolveFeatureMacroFunctions(String formula) {
        return super.resolveFeatureMacroFunctions(formula)
                .replaceAll("ENABLED\\s*\\(([^)]*)\\)", "$1")
                .replaceAll("DISABLED\\s*\\(([^)]*)\\)", "!($1)");
    }
}

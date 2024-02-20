package org.variantsync.diffdetective.feature;

import org.variantsync.diffdetective.error.UnparseableFormulaException;

/**
 * Extracts the expression from a C preprocessor statement.
 * For example, given the annotation "#if defined(A) || B()", the extractor would extract
 * "A || B". The extractor detects if, ifdef, ifndef and elif annotations.
 * (Other annotations do not have expressions.)
 * The given pre-processor statement might also a line in a diff (i.e., preceeded by a - or +).
 *
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr, Alexander Schultheiß
 */
public interface DiffLineFormulaExtractor {
    /**
     * Extracts the feature formula as a string from a macro line (possibly within a diff).
     *
     * @param line The line of which to get the feature mapping
     * @return The feature mapping as a String of the given line
     */
    String extractFormula(final String line) throws UnparseableFormulaException;

    /**
     * Resolves any macros in the given formula that are relevant for feature annotations.
     * For example, in {@link org.variantsync.diffdetective.datasets.predefined.MarlinCPPDiffLineFormulaExtractor Marlin},
     * feature annotations are given by the custom <code>ENABLED</code> and <code>DISABLED</code> macros,
     * which have to be unwrapped.
     *
     * @param formula The formula whose feature macros to resolve.
     * @return The parseable formula as string. The default implementation returns the input string.
     */
    default String resolveFeatureMacroFunctions(String formula) {
        return formula;
    }
}

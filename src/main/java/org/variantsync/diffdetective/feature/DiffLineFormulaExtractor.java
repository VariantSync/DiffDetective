package org.variantsync.diffdetective.feature;

import org.variantsync.diffdetective.error.UnparseableFormulaException;

/**
 * Interface for extracting a formula from a line containing an annotation.
 * The line might be preceded by a '-', '+', or ' '.
 * For example, given the line "+#if defined(A) || B()", the extractor should extract "defined(A) || B".
 *
 * <p>
 * Further alterations of the extracted formula are allowed. For instance, the extracted formula might be abstracted
 * (e.g., by simplifying the call to "defined(A)" leaving only the argument "A", or substituting it with "DEFINED_A").
 * </p>
 *
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr, Alexander Schultheiß
 */
public interface DiffLineFormulaExtractor {
    /**
     * Extracts the feature formula as a string from a line (possibly within a diff).
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

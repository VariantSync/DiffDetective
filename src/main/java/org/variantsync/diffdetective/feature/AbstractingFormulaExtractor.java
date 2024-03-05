package org.variantsync.diffdetective.feature;

import org.tinylog.Logger;
import org.variantsync.diffdetective.error.UncheckedUnParseableFormulaException;
import org.variantsync.diffdetective.error.UnparseableFormulaException;
import org.variantsync.diffdetective.feature.cpp.AbstractingCExpressionVisitor;
import org.variantsync.diffdetective.feature.cpp.ControllingCExpressionVisitor;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AbstractingFormulaExtractor is an abstract class that extracts a formula from text containing a conditional annotation,
 * and then abstracts the formula using the custom {@link #abstractFormula(String)} implementation of its subclass.
 * The extraction of a formula is controlled by a {@link Pattern} with which an AbstractingFormulaExtractor is initialized.
 * The given text might also be a line in a diff (i.e., preceeded by a '-' or '+').
 *
 * <p>
 * For example, given the annotation "#if defined(A) || B()", the extractor should extract the formula
 * "defined(A) || B". It would then hand this formula to the {@link #abstractFormula(String)} method for abstraction
 * (e.g., to substitute the 'defined(A)' macro call with 'DEFINED_A').
 * </p>
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr, Alexander Schultheiß
 */
public abstract class AbstractingFormulaExtractor implements DiffLineFormulaExtractor {
    private final Pattern annotationPattern;

    /**
     * Initialize a new AbstractingFormulaExtractor object that uses the given Pattern to identify formulas in annotations.
     * See {@link org.variantsync.diffdetective.feature.cpp.CPPDiffLineFormulaExtractor} for an example of how such a pattern
     * could look like.
     * @param annotationPattern The pattern used for formula extraction
     */
    public AbstractingFormulaExtractor(Pattern annotationPattern) {
        this.annotationPattern = annotationPattern;
    }

    /**
     * Extracts the feature formula as a string from a piece of text (possibly within a diff) and abstracts it.
     *
     * @param text The text of which to extract the formula
     * @return The extracted and abstracted formula
     */
    @Override
    public String extractFormula(final String text) throws UnparseableFormulaException {
        final Matcher matcher = annotationPattern.matcher(text);
        final Supplier<UnparseableFormulaException> couldNotExtractFormula = () ->
                new UnparseableFormulaException("Could not extract formula from line \"" + text + "\".");

        // Retrieve the formula from the macro line
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

        // abstract complex formulas (e.g., if they contain arithmetics or macro calls)
        try {
            fm = abstractFormula(fm);
        } catch (UncheckedUnParseableFormulaException e) {
            throw e.inner();
        } catch (Exception e) {
            Logger.warn(e);
            throw new UnparseableFormulaException(e);
        }

        if (fm.isEmpty()) {
            throw couldNotExtractFormula.get();
        }

        return fm;
    }

    /**
     * Abstract the given formula (e.g., by substituting parts of the formula with predefined String literals).
     * See {@link org.variantsync.diffdetective.feature.cpp.CPPDiffLineFormulaExtractor} for an example of how this could
     * be done.
     *
     * @param formula that is to be abstracted
     * @return the abstracted formula
     */
    protected abstract String abstractFormula(String formula);
}

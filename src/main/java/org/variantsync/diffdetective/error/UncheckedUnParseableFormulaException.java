package org.variantsync.diffdetective.error;

/**
 * Runtime exception for cases in which a formula extracted from a diff cannot be parsed.
 */
public class UncheckedUnParseableFormulaException extends RuntimeException {
    final UnparseableFormulaException inner;

    public UncheckedUnParseableFormulaException(String message, Exception e) {
        super(message, e);
        inner = new UnparseableFormulaException(message, e);
    }

    public UnparseableFormulaException inner() {
        return this.inner;
    }
}

package org.variantsync.diffdetective.error;

/**
 * Runtime exception for cases in which a formula extracted from a diff cannot be parsed.
 */
public class UncheckedUnParseableFormulaException extends RuntimeException {
    final UnParseableFormulaException inner;

    public UncheckedUnParseableFormulaException(String message, Exception e) {
        super(message, e);
        inner = new UnParseableFormulaException(message, e);
    }

    public UnParseableFormulaException inner() {
        return this.inner;
    }
}

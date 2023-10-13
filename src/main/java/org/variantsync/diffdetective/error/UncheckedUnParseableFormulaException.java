package org.variantsync.diffdetective.error;

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

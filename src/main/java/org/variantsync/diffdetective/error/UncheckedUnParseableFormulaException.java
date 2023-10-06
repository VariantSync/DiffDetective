package org.variantsync.diffdetective.error;

public class UncheckedUnParseableFormulaException extends RuntimeException {
    final UnParseableFormulaException inner;

    public UncheckedUnParseableFormulaException(String message, Exception e) {
        super(message, e);
        inner = new UnParseableFormulaException(message, e);
    }

    public UncheckedUnParseableFormulaException(String message) {
        super(message);
        inner = new UnParseableFormulaException(message);
    }

    public UncheckedUnParseableFormulaException(Exception e) {
        super(e);
        inner = new UnParseableFormulaException(e);
    }

    public UnParseableFormulaException inner() {
        return this.inner;
    }
}

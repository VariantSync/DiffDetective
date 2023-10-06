package org.variantsync.diffdetective.error;

public class UnParseableFormulaException extends Exception {
    public UnParseableFormulaException(Exception other) {
        super(other);
    }

    public UnParseableFormulaException(String message) {
        super(message);
    }

    public UnParseableFormulaException(String message, Exception other) {
        super(message, other);
    }
}

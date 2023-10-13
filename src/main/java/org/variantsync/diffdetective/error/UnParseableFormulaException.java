package org.variantsync.diffdetective.error;

/**
 * Exception for cases in which a formula extracted from a diff cannot be parsed.
 */
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

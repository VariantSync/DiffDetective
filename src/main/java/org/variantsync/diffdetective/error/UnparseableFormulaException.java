package org.variantsync.diffdetective.error;

/**
 * Exception for cases in which a formula extracted from a diff cannot be parsed.
 */
public class UnparseableFormulaException extends Exception {
    public UnparseableFormulaException(Exception other) {
        super(other);
    }

    public UnparseableFormulaException(String message) {
        super(message);
    }

    public UnparseableFormulaException(String message, Exception other) {
        super(message, other);
    }
}

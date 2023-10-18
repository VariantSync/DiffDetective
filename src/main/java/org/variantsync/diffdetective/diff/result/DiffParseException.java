package org.variantsync.diffdetective.diff.result;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;

/**
 * Describes an error that occurred when processing a text-based diff.
 * @author Benjamin Moosherr
 */
public final class DiffParseException extends Exception {
    private final DiffError error;
    private final DiffLineNumber lineNumber;

    /**
     * @param error the error type to be reported
     * @param lineNumber the source line of the error
     */
    public DiffParseException(DiffError error, DiffLineNumber lineNumber) {
        this.error = error;
        this.lineNumber = lineNumber;
    }

    /**
     * @param exception an exception that indicated the error
     * @param error the error type to be reported
     * @param lineNumber the source line of the error
     */
    private DiffParseException(Exception exception, DiffError error, DiffLineNumber lineNumber) {
        super(exception);
        this.error = error;
        this.lineNumber = lineNumber;
    }

    /**
     * A DiffParseException due to an unparseable formula.
     * @param exception The parse exception that occurred
     * @param lineNumber The line number of the unparseable formula
     */
    public static DiffParseException Unparseable(Exception exception, DiffLineNumber lineNumber) {
        return new DiffParseException(exception, DiffError.UNPARSEABLE_FORMULA, lineNumber);
    }

    public DiffError getError() {
        return error;
    }

    public DiffLineNumber getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "DiffParseException on line " + lineNumber + ": " + error;
    }
}

package org.variantsync.diffdetective.diff.result;

import org.variantsync.diffdetective.diff.DiffLineNumber;

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

package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.result.DiffError;

/**
 * Exception that is thrown when a syntactically incorrect feature annotation is found.
 * @author Paul Bittner
 */
public class IllFormedAnnotationException extends Exception {
    private final DiffError errorType;

    /**
     * Create an exception for reporting the given error.
     * @param errorType The error to report.
     * @param msg The message for this exception.
     * @see Exception#Exception(String)
     */
    private IllFormedAnnotationException(DiffError errorType, String msg) {
        super(msg);
        this.errorType = errorType;
    }

    /**
     * Creates an IllFormedAnnotationException with the given message and the error
     * {@link DiffError#IF_WITHOUT_CONDITION}.
     * @param message The message of the resulting exception.
     * @return An exception reporting that an if annotation without a condition was found.
     */
    public static IllFormedAnnotationException IfWithoutCondition(String message) {
        return new IllFormedAnnotationException(DiffError.IF_WITHOUT_CONDITION, message);
    }

    /**
     * Returns the error that is reported by this exception.
     */
    public DiffError getType() {
        return errorType;
    }
}

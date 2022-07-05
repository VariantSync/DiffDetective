package org.variantsync.diffdetective.diff.difftree;

/**
 * An exception that signals that an invalid {@link Time} was used for a certain computation.
 */
public class WrongTimeException extends RuntimeException {
    /**
     * Creates a new exception with the given error message.
     * @param message Description of why the error occurred.
     */
    public WrongTimeException(final String message) {
        super(message);
    }
}

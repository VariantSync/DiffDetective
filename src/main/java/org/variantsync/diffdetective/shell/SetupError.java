package org.variantsync.diffdetective.shell;

/**
 * Thrown if a precondition for setting up the execution of a command isn't met.
 *
 * <p>Example of such a precondition: Requiring a UNIX like operating system.
 *
 * @author Alexander Schultheiß
 */
public class SetupError extends Error {
    public SetupError(String errorMessage) {
        super(errorMessage);
    }
}

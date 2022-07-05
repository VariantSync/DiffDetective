package org.variantsync.diffdetective.shell;

import java.util.Arrays;
import java.util.List;

/**
 * Representation of an executable command.
 * An <i>executable command</i> is a path to an executable file with arguments which are provided
 * to that executable on execution.
 *
 * @author Alexander Schultheiß
 */
public abstract class ShellCommand {
    /**
     * Returns the String parts that define and configure the command execution.
     * The first array entry is a path to an executable file. The others are arguments for that
     * executable.
     *
     * <p>Example: ["echo", "Hello World"]
     *
     * @return the parts of the shell command.
     */
    public abstract String[] parts();

    /**
     * Interpret the result/exit code returned from a shell command.
     * An {@code ShellException} is thrown if the result code is an error.
     *
     * @param resultCode the code that is to be parsed
     * @param output the output of the shell command
     * @return the output of the shell command
     * @throws ShellException if {@code resultCode} is an error
     */
    public List<String> interpretResult(int resultCode, List<String> output) throws ShellException {
        if (resultCode == 0) {
            return output;
        }

        throw new ShellException(output);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.parts());
    }
}

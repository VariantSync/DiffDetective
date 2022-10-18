package org.variantsync.diffdetective.shell;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Thrown on execution errors of {@code ShellCommand}s.
 * Causes include missing executables as well as failed commands.
 *
 * @author Alexander Schultheiß
 */
public class ShellException extends Exception {
    private final List<String> output;

    /** Constructs a {@code ShellException} on command failures indicated by an exception. */
    public ShellException(Exception e) {
        super(e);
        this.output = new LinkedList<>();
    }

    /**
     * Constructs a {@code ShellException} on command failures indicated by an exit code.
     * If possible both {@code stdout} and {@code stderr} should be provided in {@code output}.
     */
    public ShellException(List<String> output) {
        super(convert(output));
        this.output = output;
    }

    /** Returns the output ({@code stdout} and {@code stderr}) of the failed command. */
    public List<String> getOutput() {
        return output;
    }

    /**
     * Merge lines into a single string.
     * Uses {@code System.lineSeparator} for separating the lines. A final new line is added only
     * if at least one line is given.
     *
     * @param output lines to be merged
     * @return a single {@code String} containing all lines of {@code output}
     */
    private static String convert(Collection<String> output) {
        StringBuilder sb = new StringBuilder();
        output.forEach(l -> sb.append(l).append(System.lineSeparator()));
        return sb.toString();
    }
}

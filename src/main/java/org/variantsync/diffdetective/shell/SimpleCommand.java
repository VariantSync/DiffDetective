package org.variantsync.diffdetective.shell;

/** Single executable command with arguments. */
public class SimpleCommand extends ShellCommand {
    private final String[] parts;

    /**
     * Constructs a single command.
     * The first argument has to be a path to an executable which will be given all of the
     * remaining arguments as parameters on execution.
     *
     * @param cmd executable path and arguments for the executable
     */
    public SimpleCommand(final String... cmd) {
        parts = cmd;
    }

    @Override
    public String[] parts() {
        return parts;
    }
}

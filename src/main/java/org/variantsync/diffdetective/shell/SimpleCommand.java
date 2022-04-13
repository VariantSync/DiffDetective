package org.variantsync.diffdetective.shell;

public class SimpleCommand extends ShellCommand {
    private final String[] parts;

    public SimpleCommand(final String... cmd) {
        parts = cmd;
    }

    @Override
    public String[] parts() {
        return parts;
    }
}

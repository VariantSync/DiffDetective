package org.variantsync.diffdetective.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompositeCommand extends ShellCommand {
    private static final String COMMAND_COMPOSE_OPERATOR = " && ";

    private final List<ShellCommand> commands;

    public CompositeCommand(ShellCommand... commands) {
        this.commands = Arrays.asList(commands);
    }

    @Override
    public String[] parts() {
        final ArrayList<String> cmd = new ArrayList<>();

        boolean first = true;
        for (final ShellCommand inner : commands) {
            if (first) {
                first = false;
            } else {
                cmd.add(COMMAND_COMPOSE_OPERATOR);
            }

            Collections.addAll(cmd, inner.parts());
        }

        return cmd.toArray(new String[cmd.size()]);
    }
}

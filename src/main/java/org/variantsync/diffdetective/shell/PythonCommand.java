package org.variantsync.diffdetective.shell;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PythonCommand extends ShellCommand {
    public static String DiffDetectiveVenv = ".venv/bin/python3";
    private final String pythonName;
    private final Path script;
    private final List<String> args;

    public PythonCommand(final String commandName, final Path script) {
        this.pythonName = commandName;
        this.script = script;
        this.args = new ArrayList<>();
    }

    public PythonCommand addArg(final String arg) {
        args.add(arg);
        return this;
    }

    public PythonCommand addArg(final Object o) {
        return addArg(o.toString());
    }

    public static PythonCommand Python(final Path script) {
        return new PythonCommand("python", script);
    }

    public static PythonCommand Python3(final Path script) {
        return new PythonCommand("python3", script);
    }

    public static PythonCommand DiffDetectiveVenvPython3(Path script) {
        return new PythonCommand(DiffDetectiveVenv, script);
    }

    @Override
    public String[] parts() {
        final String[] cmd = new String[2 + args.size()];
        cmd[0] = pythonName;
        cmd[1] = script.toString();

        int i = 2;
        for (final String arg : args) {
            cmd[i] = arg;
            ++i;
        }
        return cmd;
    }
}

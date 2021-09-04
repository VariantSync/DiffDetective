package shell;

import java.nio.file.Path;

public class PythonCommand extends ShellCommand {
    private final String pythonName;
    private final Path script;
    private final String[] args;

    public PythonCommand(final String commandName, final Path script, final String... args) {
        this.pythonName = commandName;
        this.script = script;
        this.args = args;
    }

    public static PythonCommand Python3(final Path script, final String... args) {
        return new PythonCommand("python3", script, args);
    }

    public static PythonCommand Python(final Path script, final String... args) {
        return new PythonCommand("python", script, args);
    }

    public static PythonCommand VenvPython3(Path script, String... args) {
        return new PythonCommand(".venv/bin/python3", script, args);
    }

    @Override
    public String[] parts() {
        final String[] cmd = new String[2 + args.length];
        cmd[0] = pythonName;
        cmd[1] = script.toString();
        System.arraycopy(args, 0, cmd, 2, args.length);
        return cmd;
    }
}

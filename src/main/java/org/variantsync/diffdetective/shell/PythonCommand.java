package org.variantsync.diffdetective.shell;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Command for running a python script with arguments.
 * This class provides a builder like API to add arguments passed to the python script which can be
 * interpreted by a specified python version.
 */
public class PythonCommand extends ShellCommand {
    /**
     * Path to the python interpreter of the virtual environment (relative to the project/Git
     * root).
     */
    public static String DiffDetectiveVenv = ".venv/bin/python3";
    private final String pythonName;
    private final Path script;
    private final List<String> args;

    /**
     * Constructs a shell command running a python script with arguments.
     *
     * @param commandName a path to the python interpreter used to interpret {@code script}
     * @param script the python script to be executed
     */
    public PythonCommand(final String commandName, final Path script) {
        this.pythonName = commandName;
        this.script = script;
        this.args = new ArrayList<>();
    }

    /** Pass the additional argument {@code arg} to the python script. */
    public PythonCommand addArg(final String arg) {
        args.add(arg);
        return this;
    }

    /** Pass the additional argument {@code o.toString()} to the python script. */
    public PythonCommand addArg(final Object o) {
        return addArg(o.toString());
    }

    /**
     * Constructs a python command interpreting {@code script} with the default python version
     * installed.
     * Note that this should only be used for testing or in a reproducible environment (for example
     * Docker) to ensure correct functionality on all supported platforms.
     *
     * @see DiffDetectiveVenvPython3
     */
    public static PythonCommand Python(final Path script) {
        return new PythonCommand("python", script);
    }

    /**
     * Constructs a python command interpreting {@code script} with the installed python 3 version.
     * Note that this should only be used for testing or in a reproducible environment (for example
     * Docker) to ensure correct functionality on all supported platforms.
     *
     * @see DiffDetectiveVenvPython3
     */
    public static PythonCommand Python3(final Path script) {
        return new PythonCommand("python3", script);
    }

    /**
     * Constructs a python command interpreting {@code script} with the python version installed in
     * the virtual environment of DiffDetective.
     * It should be safe to use this python version on all supported platform given that the
     * virtual environment was set up correctly, which this method just assumes without checking.
     *
     * @see DiffDetectiveVenv
     */
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

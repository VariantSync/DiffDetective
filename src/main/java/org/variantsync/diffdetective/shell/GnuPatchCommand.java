package org.variantsync.diffdetective.shell;

import java.util.List;

/** Single executable command with arguments. */
public class GnuPatchCommand extends ShellCommand {
    private final String[] parts;
    private boolean patchingSuccessful;

    /**
     * Constructs a single command.
     * The first argument has to be a path to an executable which will be given all of the
     * remaining arguments as parameters on execution.
     *
     * @param cmd executable path and arguments for the executable
     */
    public GnuPatchCommand(final String... cmd) {
        parts = cmd;
    }

    @Override
    public String[] parts() {
        return parts;
    }
    
    /**
     * Interpret the result/exit code returned from a shell command.
     * An {@code ShellException} is thrown if the result code is an error.
     *
     * @param resultCode the code that is to be parsed
     * @param output the output of the shell command
     * @return the output of the shell command
     * @throws ShellException if {@code resultCode} is an error
     */
    @Override
    public List<String> interpretResult(int resultCode, List<String> output) throws ShellException {
    	System.out.println(resultCode);
    	// patching was successful
        if (resultCode == 0) {
        	patchingSuccessful = true;
            return null;
        }
        // some hunks cannot be applied or merge conflicts
        if (resultCode == 1) {
        	patchingSuccessful = false;
        	return null;
        }
        // everything else: "serious trouble": exit code 2
        throw new ShellException(output);
              
    }

	public boolean isPatchingSuccessful() {
		return patchingSuccessful;
	}
}

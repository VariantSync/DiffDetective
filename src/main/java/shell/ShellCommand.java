package shell;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alexander Schultheiß
 */
public abstract class ShellCommand {
    /***
     * Return the String parts that define and configure the command execution (e.g., ["echo", "Hello World"])
     *
     * @return the parts of the shell command.
     */
    public abstract String[] parts();

    /**
     * Interpret the result code returned from a shell command
     *
     * @param resultCode the code that is to be parsed
     * @return the result
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

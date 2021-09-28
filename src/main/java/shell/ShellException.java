package shell;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Schultheiß
 */
public class ShellException extends Exception {
    private final List<String> output;

    public ShellException(Exception e) {
        super(e);
        this.output = new LinkedList<>();
    }

    public ShellException(List<String> output) {
        super(convert(output));
        this.output = output;
    }

    public List<String> getOutput() {
        return output;
    }

    private static String convert(Collection<String> output) {
        StringBuilder sb = new StringBuilder();
        output.forEach(l -> sb.append(l).append(System.lineSeparator()));
        return sb.toString();
    }
}

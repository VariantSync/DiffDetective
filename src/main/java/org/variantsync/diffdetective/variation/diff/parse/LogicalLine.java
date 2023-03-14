package org.variantsync.diffdetective.variation.diff.parse;

import java.util.ArrayList;
import java.util.List;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * A logical line consisting of multiple physical lines of a text file joined by line continuations.
 *
 * The concept of a logical line is used by the C11 standard to refer to one source code line of a
 * source file after all line continuations have been eliminated. Essentially a logical line is a
 * string matched by the regex {@code ([^\n]|\\\n)*} where {@code \n} represents the platform
 * dependent line delimiter. In contrast, a physical line is matched by the regex {@code [^\n]*}.
 *
 * @author Benjamin Moosherr
 */
class LogicalLine {
    private List<DiffNode.Label.Line> lines;
    private boolean isContinued;
    private DiffLineNumber startLineNumber;

    /**
     * Constructs an empty logical line.
     */
    public LogicalLine() {
        reset();
    }

    /**
     * Starts a new, empty logical line.
     * This especially ensures {@code hasStarted() == false} and {@code isComplete() == false}.
     */
    public void reset() {
        lines = new ArrayList<>();
        isContinued = false;
        startLineNumber = DiffLineNumber.Invalid();
    }

    /**
     * Adds the physical line {@code line} with the line number {@code lineNumber} to this logical
     * line.
     * This must not be called while {@code isComplete()} returns {@code true}. There must be no
     * new line inside of {@code line}.
     */
    public void consume(String line, DiffLineNumber lineNumber) {
        Assert.assertTrue(!isComplete());

        if (!hasStarted()) {
            startLineNumber = lineNumber;
        }
        lines.add(new DiffNode.Label.Line(line, lineNumber));
        isContinued = line.endsWith("\\");
    }

    /**
     * Returns {@code true} iff at least one physical line was {@link consume}d.
     */
    public boolean hasStarted() {
        return !lines.isEmpty();
    }

    /**
     * Returns {@code true} iff the current logical is complete and the next physical line belongs
     * to a new logical line.
     */
    public boolean isComplete() {
        return hasStarted() && !isContinued;
    }

    /**
     * Returns the line number of the first {@link consume}d physical line of the current physical
     * line.
     */
    public DiffLineNumber getStartLineNumber() {
        return startLineNumber;
    }

    /**
     * Returns all physical lines {@link consume}d for the current logical line.
     * The backslashes of line continuations are still part of the strings.
     */
    public List<DiffNode.Label.Line> getLines() {
        return lines;
    }

    /**
     * Returns this line without line continuations.
     */
    @Override
    public String toString() {
        var logicalLine = new StringBuilder();
        for (var it = lines.iterator(); it.hasNext(); ) {
            String physicalLine = it.next().content();
            // Remove the backslash of the line continuation
            logicalLine.append(physicalLine, 0, physicalLine.length() - (it.hasNext() ? 1 : 0));
        }

        return logicalLine.toString();
    }
}

package org.variantsync.diffdetective.variation.diff.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.DiffLinesLabel;

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
    private List<DiffLinesLabel.Line> lines;
    private boolean isContinued;
    private boolean inComment;
    private DiffLineNumber startLineNumber;

    private final static Pattern LINE_COMMENT = Pattern.compile("[^(/*)]*?//[^(*/)]*?");

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
        inComment = false;
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

        // Handle line continuations
        isContinued = line.endsWith("\\");

        // Handle multi-line inline macros
        int commentStart = line.lastIndexOf("/*");
        int commentEnd = line.lastIndexOf("*/");
        Matcher lineCommentMatcher = LINE_COMMENT.matcher(line);
        int lineCommentStart = lineCommentMatcher.find() ? lineCommentMatcher.start() : -1;
        if (commentStart != -1 || commentEnd != -1) {
            // Update 'inComment' if a C-style comment is opened and/or closed
            if (lineCommentStart != -1 && lineCommentStart < commentStart) {
                // Handle cases in which comments are embedded in other comments
                // e.g., '// /* some comment'
                inComment = false;
            } else {
                // Update the value if a start and/or end have been found
                // The line is part of a multi-line comment, if a comment starts in this line (after another comment ends)
                inComment = commentStart > commentEnd;
            }
        }

        lines.add(new DiffLinesLabel.Line(line, lineNumber));
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
        return hasStarted() && !isContinued && !inComment;
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
    public List<DiffLinesLabel.Line> getLines() {
        return lines;
    }

    /**
     * Returns this line without line continuations.
     */
    @Override
    public String toString() {
        var logicalLine = new StringBuilder();
        for (DiffLinesLabel.Line line : lines) {
            String physicalLine = line.content();
            // Remove the backslash of the line continuation
            logicalLine.append(physicalLine, 0, physicalLine.length() - (physicalLine.endsWith("\\") ? 1 : 0));
        }

        return logicalLine.toString();
    }
}

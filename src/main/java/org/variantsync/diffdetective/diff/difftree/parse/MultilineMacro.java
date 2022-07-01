package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a multiline macro definition in a diff during parsing of a text-based diff.
 * @author Paul Bittner
 */
public class MultilineMacro {
    private final List<String> lines;
    private final DiffNode beforeParent;
    private final DiffNode afterParent;
    private final DiffLineNumber startLine = DiffLineNumber.Invalid();
    DiffType diffType;

    /**
     * Create the definition of a multiline macro from the given values.
     * @param line The corresponding line in the diff.
     * @param diffType The diff type of this multiline macro.
     * @param startLine The line number at which the definition of this macro began.
     * @param beforeParent The parent of this macro before the edit.
     * @param afterParent The parent of this macro after the edit.
     */
    public MultilineMacro(
            final String line,
            final DiffType diffType,
            final DiffLineNumber startLine,
            final DiffNode beforeParent,
            final DiffNode afterParent) {
        this.lines = new ArrayList<>();
        this.lines.add(line);
        this.diffType = diffType;
        this.startLine.set(startLine);
        this.beforeParent = beforeParent;
        this.afterParent = afterParent;
    }

    /**
     * Returns the line number at which the definition of this macro began.
     */
    public DiffLineNumber getLineFrom() {
        return startLine;
    }

    /**
     * Adds the given line to this multiline macro.
     * The given line is considered to be part of this multiline macro's definition.
     */
    public void addLine(final String line) {
        lines.add(line);
    }

    /**
     * Parses this multiline macro definition in a DiffNode.
     * @param nodeParser This parse is used to create the DiffNode after all lines of this
     *                   multiline macro are joined to a single line.
     * @return A DiffNode representing this multiline macro.
     * @throws IllFormedAnnotationException when {@link DiffNodeParser#fromDiffLine(String)} fails.
     */
    public DiffNode toDiffNode(DiffNodeParser nodeParser) throws IllFormedAnnotationException {
        final StringBuilder asSingleLine = new StringBuilder(diffType.symbol);

        for (int l = 0; l < lines.size(); ++l) {
            final String line = lines.get(l);
            if (l < lines.size() - 1) {
                asSingleLine.append(line.substring(1, line.lastIndexOf('\\')).trim()).append(" ");
            } else {
                asSingleLine.append(line.substring(1).trim());
            }
        }

        final DiffNode result = nodeParser.fromDiffLine(asSingleLine.toString());
        result.getFromLine().set(startLine);
        result.addBelow(beforeParent, afterParent);
        result.setIsMultilineMacro(true);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultilineMacro that = (MultilineMacro) o;
        return startLine.equals(that.startLine) && lines.equals(that.lines) && diffType == that.diffType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, diffType, startLine);
    }

    @Override
    public String toString() {
        return "MultilineMacro{" +
                "lines=" + lines +
                ", beforeParent=" + beforeParent +
                ", afterParent=" + afterParent +
                ", startLine=" + startLine +
                ", diffType=" + diffType +
                '}';
    }
}

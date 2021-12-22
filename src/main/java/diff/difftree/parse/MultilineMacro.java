package diff.difftree.parse;

import diff.DiffLineNumber;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultilineMacro {
    private final List<String> lines;
    private final DiffNode beforeParent;
    private final DiffNode afterParent;
    private final DiffLineNumber startLine = DiffLineNumber.Invalid();
    DiffType diffType;

    public MultilineMacro(
            final String header,
            final DiffLineNumber startLine,
            final DiffNode beforeParent,
            final DiffNode afterParent) {
        this(
                header, DiffType.ofDiffLine(header),
                startLine,
                beforeParent, afterParent);
    }

    private MultilineMacro(
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

    public DiffLineNumber getLineFrom() {
        return startLine;
    }

    public void addLine(final String line) {
        lines.add(line);
    }

    public DiffNode toDiffNode() throws IllFormedAnnotationException {
        final StringBuilder asSingleLine = new StringBuilder(diffType.symbol);

        for (int l = 0; l < lines.size(); ++l) {
            final String line = lines.get(l);
            if (l < lines.size() - 1) {
                asSingleLine.append(line.substring(1, line.lastIndexOf('\\')).trim()).append(" ");
            } else {
                asSingleLine.append(line.substring(1).trim());
            }
        }

        final DiffNode result = DiffNode.fromDiffLine(asSingleLine.toString());
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
        return startLine == that.startLine && lines.equals(that.lines) && diffType == that.diffType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, diffType, startLine);
    }
}

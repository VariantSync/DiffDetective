package diff.difftree.parse;

import diff.difftree.DiffNode;
import diff.difftree.DiffType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultilineMacro {
    private final List<String> lines;
    private final DiffNode beforeParent;
    private final DiffNode afterParent;

    DiffType diffType;
    DiffLineNumber startLine;

    public MultilineMacro(
            String header,
            DiffLineNumber startLine,
            DiffNode beforeParent,
            DiffNode afterParent) {
        this(
                header, DiffType.ofDiffLine(header),
                startLine,
                beforeParent, afterParent);
    }

    private MultilineMacro(
            String line,
            DiffType diffType,
            DiffLineNumber startLine,
            DiffNode beforeParent,
            DiffNode afterParent) {
        this.lines = new ArrayList<>();
        lines.add(line);
        this.diffType = diffType;
        this.startLine = startLine;
        this.beforeParent = beforeParent;
        this.afterParent = afterParent;
    }

    public DiffLineNumber getLineFrom() {
        return startLine;
    }

    public void addLine(final String line) {
        lines.add(line);
    }

    public DiffNode toDiffNode() {
        final StringBuilder asSingleLine = new StringBuilder(diffType.name);

        for (int l = 0; l < lines.size(); ++l) {
            String line = lines.get(l);
            if (l < lines.size() - 1) {
                asSingleLine.append(line, 1, line.lastIndexOf('\\'));
            } else {
                asSingleLine.append(line.substring(1));
            }
        }

        final DiffNode result = DiffNode.fromDiffLine(asSingleLine.toString(), beforeParent, afterParent);
        result.getFromLine().set(startLine);
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

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
    int startLineInDiff, endLineInDiff;

    public MultilineMacro(String header, int lineFrom, DiffNode beforeParent, DiffNode afterParent) {
        this(
                header,
                DiffType.ofDiffLine(header),
                lineFrom, beforeParent, afterParent);
    }

    private MultilineMacro(String line, DiffType diffType, int lineFrom, DiffNode beforeParent, DiffNode afterParent) {
        this.lines = new ArrayList<>();
        lines.add(line);
        this.diffType = diffType;
        this.startLineInDiff = lineFrom;
        this.beforeParent = beforeParent;
        this.afterParent = afterParent;
    }

    public int getLineFrom() {
        return startLineInDiff;
    }

    public int getLineTo() {
        return endLineInDiff;
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
        result.getLinesInDiff().setFromInclusive(startLineInDiff);
        result.getLinesInDiff().setToExclusive(endLineInDiff);
        result.setIsMultilineMacro(true);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultilineMacro that = (MultilineMacro) o;
        return startLineInDiff == that.startLineInDiff && lines.equals(that.lines) && diffType == that.diffType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines, diffType, startLineInDiff);
    }
}

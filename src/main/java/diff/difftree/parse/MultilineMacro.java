package diff.difftree.parse;

import diff.difftree.DiffNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultilineMacro {
    public List<String> lines;
    DiffNode.DiffType diffType;
    int startLineInDiff, endLineInDiff;
    DiffNode beforeParent, afterParent;

    public MultilineMacro(String header, int lineFrom, DiffNode beforeParent, DiffNode afterParent) {
        this(
                header,
                DiffNode.getDiffType(header),
                lineFrom, beforeParent, afterParent);
    }

    private MultilineMacro(String line, DiffNode.DiffType diffType, int lineFrom, DiffNode beforeParent, DiffNode afterParent) {
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

    public boolean nextLine(String currentLine, int lineNo) {
        lines.add(currentLine);

        if (MultiLineMacroParser.continuesMultilineDefinition(currentLine)) {
            return false;
        } else {
            endLineInDiff = lineNo;
            return true;
        }
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

        final DiffNode result = DiffNode.fromLine(asSingleLine.toString(), beforeParent, afterParent);
        result.setFromLine(startLineInDiff);
        result.setToLine(endLineInDiff);
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

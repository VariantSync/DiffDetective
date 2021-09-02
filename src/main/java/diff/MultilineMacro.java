package diff;

import diff.data.DiffNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultilineMacro {
    public List<String> lines;
    final DiffNode.DiffType diffType;
    int startLineInDiff, endLineInDiff;

    public MultilineMacro(String header, int lineFrom) {
        this(
                header,
                DiffNode.getDiffType(header),
                lineFrom);
    }

    private MultilineMacro(String line, DiffNode.DiffType diffType, int lineFrom) {
        this.lines = new ArrayList<>();
        lines.add(line);
        this.diffType = diffType;
        this.startLineInDiff = lineFrom;
    }

    public int getLineFrom() {
        return startLineInDiff;
    }

    public int getLineTo() {
        return endLineInDiff;
    }

    public static boolean continuesMultilineDefinition(String line) {
        return line.trim().endsWith("\\");
    }

    public boolean nextLine(String currentLine, int lineNo) {
        lines.add(currentLine);

        if (MultilineMacro.continuesMultilineDefinition(currentLine)) {
//            // 1 to remove diff symbol at beginning
//            asSingleLine += " " + currentLine.substring(1, currentLine.lastIndexOf('\\'));
            return false;
        } else {
//            asSingleLine += " " + currentLine.substring(1);
            endLineInDiff = lineNo;
            return true;
        }
    }

    public DiffNode toDiffNode(DiffNode beforeParent, DiffNode afterParent) {
        final StringBuilder asSingleLine = new StringBuilder(diffType.name);

        for (int l = 0; l < lines.size(); ++l) {
            String line = lines.get(l);
            if (l < lines.size() - 1) {
                asSingleLine.append(line, 1, line.lastIndexOf('\\'));
            } else {
                asSingleLine.append(line.substring(1));
            }
        }

        return DiffNode.fromLine(asSingleLine.toString(), beforeParent, afterParent);
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

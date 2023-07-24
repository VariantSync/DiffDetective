package org.variantsync.diffdetective.variation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree; // For Javadoc

/**
 * A label consisting of lines with {@link DiffLineNumber line number} information for each line.
 *
 * Using this label, a {@link DiffTree} encodes all information (except the original line
 * ending encoding, i.e. {@code LF} vs {@code CRLF}) necessary to reconstruct the original files
 * and, when used during construction, the line diff between these files.
 */
public class DiffLinesLabel implements Label {
    private final List<Line> lines;

    public record Line(String content, DiffLineNumber lineNumber) {
        public static Line withInvalidLineNumber(String content) {
            return new Line(content, DiffLineNumber.Invalid());
        }
    }

    public DiffLinesLabel() {
        this(new ArrayList<>());
    }

    public DiffLinesLabel(List<Line> lines) {
        this.lines = lines;
    }

    public static DiffLinesLabel withInvalidLineNumbers(List<String> lines) {
        return new DiffLinesLabel(lines.stream().map(Line::withInvalidLineNumber).toList());
    }

    public static DiffLinesLabel ofCodeBlock(String codeBlock) {
        return withInvalidLineNumbers(Arrays.asList(StringUtils.LINEBREAK_REGEX.split(codeBlock, -1)));
    }

    public void addDiffLine(Line newLine) {
        lines.add(newLine);
    }

    public void addDiffLines(List<Line> newLines) {
        lines.addAll(newLines);
    }

    public List<Line> getDiffLines() {
        return lines;
    }

    @Override
    public List<String> getLines() {
        return getDiffLines().stream().map(Line::content).toList();
    }

    @Override
    public String toString() {
        return lines
            .stream()
            .map(Line::content)
            .collect(Collectors.joining(StringUtils.LINEBREAK));
    }

    @Override
    public DiffLinesLabel clone() {
        return new DiffLinesLabel(new ArrayList<>(lines));
    }
}

package org.variantsync.diffdetective.variation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff; // For Javadoc

/**
 * A label consisting of lines with {@link DiffLineNumber line number} information for each line.
 *
 * Using this label, a {@link VariationDiff} encodes all information (except the original line
 * ending encoding, i.e. {@code LF} vs {@code CRLF}) necessary to reconstruct the original files
 * and, when used during construction, the line diff between these files.
 */
public class DiffLinesLabel implements Label {
    private final List<Line> lines;
    private List<Line> trailingLines;

    public record Line(String content, DiffLineNumber lineNumber) {
        public static Line withInvalidLineNumber(String content) {
            return new Line(content, DiffLineNumber.Invalid());
        }
    }

    public DiffLinesLabel() {
        this(new ArrayList<>());
    }

    public DiffLinesLabel(List<Line> lines) {
        this(lines, new ArrayList<>());
    }

    public DiffLinesLabel(List<Line> lines, List<Line> trailingLines) {
        Assert.assertNotNull(lines);
        Assert.assertNotNull(trailingLines);

        this.lines = lines;
        this.trailingLines = trailingLines;
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

    public void setDiffTrailingLines(List<Line> newLines) {
        Assert.assertNotNull(newLines);
        trailingLines = newLines;
    }

    public List<Line> getDiffTrailingLines() {
        return trailingLines;
    }

    @Override
    public List<String> getLines() {
        return getDiffLines().stream().map(Line::content).toList();
    }

    @Override
    public List<String> getTrailingLines() {
        return getDiffTrailingLines().stream().map(Line::content).toList();
    }

    /**
     * Returns a deep copy where the line numbers at {@code time} are set to
     * {@link DiffLineNumber#InvalidLineNumber}.
     */
    @Override
    public DiffLinesLabel withoutTimeDependentState(Time time) {
        return new DiffLinesLabel(
            mapWithoutTimeDependentState(getDiffLines(), time),
            mapWithoutTimeDependentState(getDiffTrailingLines(), time)
        );
    }

    private List<Line> mapWithoutTimeDependentState(List<Line> lines, Time time) {
        return lines
            .stream()
            .map(line -> new Line(
                line.content(),
                line.lineNumber().withLineNumberAtTime(DiffLineNumber.InvalidLineNumber, time)
            ))
            .toList();
    }

    /**
     * Returns a deep copy where the line numbers at {@code time} are copied from
     * {@code otherLabel}. The number of lines and their content must be identical in {@code this}
     * and {@code otherLabel}.
     *
     * @see withoutTimeDependentState
     */
    @Override
    public DiffLinesLabel withTimeDependentStateFrom(Label otherLabel, Time time) {
        DiffLinesLabel other = (DiffLinesLabel) otherLabel;

        return new DiffLinesLabel(
            zipWithTimeDependentStateFrom(this.getDiffLines(), other.getDiffLines(), time),
            zipWithTimeDependentStateFrom(this.getDiffTrailingLines(), other.getDiffTrailingLines(), time)
        );
    }

    private static List<Line> zipWithTimeDependentStateFrom(List<Line> linesA, List<Line> linesB, Time time) {
        List<Line> result = new ArrayList<>(linesA.size());

        Iterator<Line> itA = linesA.iterator();
        Iterator<Line> itB = linesA.iterator();
        while (itA.hasNext() && itB.hasNext()) {
            Line lineA = itA.next();
            Line lineB = itB.next();

            Assert.assertEquals(lineA.content(), lineB.content(), "Mismatching line contents in a call to `withTimeDependentStateFrom` detected");
            result.add(new Line(
                lineA.content(),
                lineB.lineNumber().withLineNumberAtTime(lineB.lineNumber().atTime(time), time)
            ));
        }

        Assert.assertFalse(itA.hasNext() || itB.hasNext(), "Mismatching line counts in a call to `withTimeDependentStateFrom` detected");

        return result;
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
        return new DiffLinesLabel(new ArrayList<>(lines), new ArrayList<>(trailingLines));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffLinesLabel that = (DiffLinesLabel) o;
        return lines.equals(that.lines) &&
            trailingLines.equals(that.trailingLines);
    }
}

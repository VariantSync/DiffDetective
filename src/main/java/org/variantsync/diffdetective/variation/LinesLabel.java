package org.variantsync.diffdetective.variation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;

/**
 * A label containing a list of lines represented as {@code String}s.
 */
public class LinesLabel implements Label {
    private final List<String> lines;
    private final List<String> trailingLines;

    public LinesLabel() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public LinesLabel(List<String> lines) {
        this(lines, new ArrayList<>());
    }

    public LinesLabel(List<String> lines, List<String> trailingLines) {
        Assert.assertNotNull(lines);
        Assert.assertNotNull(trailingLines);

        this.lines = lines;
        this.trailingLines = trailingLines;
    }

    public static LinesLabel ofCodeBlock(String codeBlock) {
        return new LinesLabel(Arrays.asList(StringUtils.LINEBREAK_REGEX.split(codeBlock, -1)));
    }

    @Override
    public List<String> getLines() {
        return lines;
    }

    @Override
    public List<String> getTrailingLines() {
        return trailingLines;
    }

    @Override
    public String toString() {
        return lines
            .stream()
            .collect(Collectors.joining(StringUtils.LINEBREAK));
    }

    @Override
    public LinesLabel clone() {
        return new LinesLabel(new ArrayList<>(lines), new ArrayList<>(trailingLines));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinesLabel that = (LinesLabel) o;
        return lines.equals(that.lines) &&
            trailingLines.equals(that.trailingLines);
    }
}

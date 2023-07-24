package org.variantsync.diffdetective.variation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.util.StringUtils;

/**
 * A label containing a list of lines represented as {@code String}s.
 */
public class LinesLabel implements Label {
    private final List<String> lines;

    public LinesLabel() {
        this(new ArrayList<>());
    }

    public LinesLabel(List<String> lines) {
        this.lines = lines;
    }

    public static LinesLabel ofCodeBlock(String codeBlock) {
        return new LinesLabel(Arrays.asList(StringUtils.LINEBREAK_REGEX.split(codeBlock, -1)));
    }

    public List<String> getLines() {
        return lines;
    }

    @Override
    public String toString() {
        return lines
            .stream()
            .collect(Collectors.joining(StringUtils.LINEBREAK));
    }

    @Override
    public LinesLabel clone() {
        return new LinesLabel(new ArrayList<>(lines));
    }
}

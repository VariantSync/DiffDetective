package org.variantsync.diffdetective.variation.diff.source;

import java.nio.file.Path;
import java.util.List;

import org.variantsync.diffdetective.util.Source;

/**
 * A source for VariationDiffs that were parsed from a linegraph file.
 * @param graphHeader The first line of the VariationDiff in the linegraph file (starting with <code>"t #</code>).
 * @param file The path to the linegraph file.
 */
public record LineGraphFileSource(
        String graphHeader,
        Path file
) implements Source {
    @Override
    public String getSourceExplanation() {
        return "LineGraphFile";
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of(graphHeader, file);
    }
}

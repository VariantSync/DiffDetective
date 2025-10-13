package org.variantsync.diffdetective.variation.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.CompositeSource;
import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;

/**
 * A {@link VariabilityAwareDiffer} that first creates a line diff and
 * {@link VariationDiffParser#createVariationDiff parses} this line diff into a
 * {@link VariationDiff}.
 */
@FunctionalInterface
public interface VariabilityAwareTextDiffer extends VariabilityAwareDiffer<DiffLinesLabel> {
    /**
     * Returns a line diff of {@code before} and {@code after} in the unified diff format.
     */
    String diffLines(Reader before, Reader after) throws IOException;

    /**
     * Returns a line diff of {@code before} and {@code after} in the unified diff format.
     */
    default String diffLines(String before, String after) throws IOException {
        return diffLines(new StringReader(before), new StringReader(after));
    }

    /**
     * Returns the options used for {@link parseDiff parsing} the {@link VariationDiff} in {@link parseDiff}.
     */
    default VariationDiffParseOptions getParseOptions() {
        return VariationDiffParseOptions.Default;
    }

    /**
     * Parses the {@link diffLines line diff} into a {@link VariationDiff} using the options
     * from {@link getParseOptions}.
     */
    default VariationDiff<DiffLinesLabel> parseDiff(String diff, Source diffSource) throws DiffParseException {
        return VariationDiffParser.createVariationDiff(
            diff,
            new CompositeSource("VariationDiffParser.createVariationDiff", diffSource),
            getParseOptions()
        );
    }

    /**
     * Composes {@link diffLines} and {@link parseDiff} to create a {@link VariationDiff}.
     */
    @Override
    default VariationDiff<DiffLinesLabel> diff(Reader before, Reader after, Source beforeSource, Source afterSource) throws IOException, DiffParseException {
        return parseDiff(
            diffLines(before, after),
            new CompositeSource("line diff", beforeSource, afterSource)
        );
    }
}

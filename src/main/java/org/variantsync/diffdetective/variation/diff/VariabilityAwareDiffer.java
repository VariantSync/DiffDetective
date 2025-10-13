package org.variantsync.diffdetective.variation.diff;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.FileSource;
import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.tree.VariationTree; // For Javadoc

/**
 * A generic interface for creating variation diffs from two states represented as text.
 * This interface represents a differ that can walk any of the paths present in our visual abstract
 * (see below) and thus the states before and after the edit are represented as text. For each of
 * the two paths, parsing a text diff and diffing variation trees, there is a specialized version of
 * this interface, {@link VariabilityAwareTextDiffer} and {@link VariabilityAwareTreeDiffer}
 * respectively. In particular, {@link VariabilityAwareTreeDiffer} represents the input states as
 * {@link VariationTree}s.
 *
 * <img alt="Variability-Aware Differencing Overview. Same as in the README.md." src="doc-files/variability-aware-differencing.png">
 *
 * @see VariabilityAwareDiffers
 */
@FunctionalInterface
public interface VariabilityAwareDiffer<L extends Label> {
    /**
     * Create a variation diff by diffing the content of two {@link Reader}s.
     * For {@link VariationDiff#getSource() traceability}, both {@link Reader}s are paired with a {@link Source}.
     */
    VariationDiff<L> diff(Reader before, Reader after, Source beforeSource, Source afterSource) throws IOException, DiffParseException;

    /**
     * Create a variation diff by diffing the content of two {@link String}s.
     * For {@link VariationDiff#getSource() traceability}, both {@link String}s are paired with a {@link Source}.
     */
    default VariationDiff<L> diff(String before, String after, Source beforeSource, Source afterSource) throws IOException, DiffParseException {
        return diff(new StringReader(before), new StringReader(after), beforeSource, afterSource);
    }

    /**
     * Create a variation diff by diffing the content of two {@link Path}s.
     * @see diff(Reader, Reader, Source, Source)
     */
    default VariationDiff<L> diff(Path before, Path after) throws IOException, DiffParseException {
        try (
            Reader beforeStream = new FileReader(before.toFile());
            Reader afterStream = new FileReader(after.toFile())
        ) {
            return diff(beforeStream, afterStream, new FileSource(before), new FileSource(after));
        }
    }
}

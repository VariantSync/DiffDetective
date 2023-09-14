package org.variantsync.diffdetective.variation.diff.serialize;

import java.io.IOException;
import java.io.OutputStream;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

/**
 * Common interface for serialisation of a single {@code VariationDiff}.
 * Not all formats have to provide a way to deserialize a {@link VariationDiff} from this format.
 *
 * @author Benjamin Moosherr
 */
public interface Exporter<L extends Label> {
    /**
     * Export a {@code variationDiff} into {@code destination}.
     *
     * This method should have no side effects besides writing to {@code destination}. Above all,
     * {@code variationDiff} shouldn't be modified. Furthermore, {@code destination} shouldn't be
     * closed to allow the embedding of the exported format into a surrounding file.
     *
     * It can be assumed, that {@code destination} is sufficiently buffered.
     *
     * @param variationDiff to be exported
     * @param destination where the result should be written
     */
    <La extends L> void exportVariationDiff(VariationDiff<La> variationDiff, OutputStream destination) throws IOException;
}

package org.variantsync.diffdetective.variation.diff.source;

import java.nio.file.Path;

/**
 * A source for VariationDiff's that were created from patch files on disk.
 * @param path Path to the patch file that was parsed to a VariationDiff.
 */
public record PatchFile(Path path) implements VariationDiffSource {
}

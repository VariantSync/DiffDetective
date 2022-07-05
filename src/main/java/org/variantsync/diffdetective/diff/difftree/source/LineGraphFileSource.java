package org.variantsync.diffdetective.diff.difftree.source;

import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

import java.nio.file.Path;

/**
 * A source for DiffTrees that were parsed from a linegraph file.
 * @param graphHeader The first line of the DiffTree in the linegraph file (starting with <code>"t #</code>).
 * @param file The path to the linegraph file.
 */
public record LineGraphFileSource(
        String graphHeader,
        Path file
) implements DiffTreeSource {
}

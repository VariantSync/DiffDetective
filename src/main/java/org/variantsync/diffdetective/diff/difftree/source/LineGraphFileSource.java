package org.variantsync.diffdetective.diff.difftree.source;

import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

import java.nio.file.Path;

public record LineGraphFileSource(
        String graphHeader,
        Path file
) implements DiffTreeSource {
}

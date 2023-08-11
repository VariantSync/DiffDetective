package org.variantsync.diffdetective.variation.tree.source;

import java.nio.file.Path;

/**
 * A reference to a file with path {@code path} in the local file system.
 */
public record LocalFileSource(Path path) implements VariationTreeSource {
    @Override
    public String toString() {
        return "file://" + path.toString();
    }
}

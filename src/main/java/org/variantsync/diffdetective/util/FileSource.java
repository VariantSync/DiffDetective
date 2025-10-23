package org.variantsync.diffdetective.util;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a file input in a {@link Source} hierarchy.
 */
public record FileSource(Path getPath) implements Source {
    @Override
    public String getSourceExplanation() {
        return "File";
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of(getPath());
    }
}

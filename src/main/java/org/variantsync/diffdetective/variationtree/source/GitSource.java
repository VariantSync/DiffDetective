package org.variantsync.diffdetective.variationtree.source;

import java.net.URL;
import java.nio.file.Path;

/**
 * A file at a specific commit in a Git repository.
 *
 * <p>The parameters of this record should be suitably chosen, so that the following commands can be
 * executed in a shell to obtain the referenced source code:
 * <code>
 *   git clone "$repository" repository
 *   cd repository
 *   git switch -d "$commitHash"
 *   cat "$path"
 * </code>
 */
public record GitSource(
    URL repository,
    String commitHash,
    Path path
) implements VariationTreeSource {
    @Override
    public String toString() {
        return path.toString() + " at " + commitHash + " of " + repository;
    }
}

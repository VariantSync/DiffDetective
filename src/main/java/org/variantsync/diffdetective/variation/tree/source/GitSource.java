package org.variantsync.diffdetective.variation.tree.source;

import java.nio.file.Path;
import java.util.List;

import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.util.Source;

/**
 * A file at a specific commit in a Git repository.
 *
 * <p>The parameters of this record should be suitably chosen, so that the following commands can be
 * executed in a shell to obtain the referenced source code:
 * <code>
 *   git clone "${repository.getRemoteURI()}" repository
 *   cd repository
 *   git switch -d "$commitHash"
 *   cat "$path"
 * </code>
 */
public record GitSource(
    Repository repository,
    String commitHash,
    Path path
) implements Source {
    @Override
    public String toString() {
        return path.toString() + " at " + commitHash + " of " + repository;
    }

    @Override
    public String getSourceExplanation() {
        return "GitCommit";
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of(repository, commitHash, path);
    }
}

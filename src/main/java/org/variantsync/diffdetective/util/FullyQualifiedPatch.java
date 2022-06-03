package org.variantsync.diffdetective.util;

import org.variantsync.diffdetective.datasets.Repository;

import java.nio.file.Path;

public record FullyQualifiedPatch(
        FullyQualifiedCommit commit,
        Path file
) {
    public FullyQualifiedPatch(
            String hash,
            Repository repo,
            Path file
    ) {
        this(new FullyQualifiedCommit(hash, repo), file);
    }
}

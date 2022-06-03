package org.variantsync.diffdetective.util;

import org.variantsync.diffdetective.datasets.Repository;

public record FullyQualifiedCommit(
        String hash,
        Repository repo
) {}
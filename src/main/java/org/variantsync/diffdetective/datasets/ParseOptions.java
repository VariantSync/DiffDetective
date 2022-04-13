package org.variantsync.diffdetective.datasets;

import org.variantsync.diffdetective.diff.difftree.parse.DiffNodeParser;

public record ParseOptions(DiffStoragePolicy diffStoragePolicy, DiffNodeParser annotationParser) {
    public enum DiffStoragePolicy {
        REMEMBER_DIFF,
        REMEMBER_STRIPPED_DIFF,
        REMEMBER_FULL_DIFF,
        DO_NOT_REMEMBER,
    }

    public ParseOptions(DiffNodeParser annotationParser) {
        this(Default.diffStoragePolicy, annotationParser);
    }

    public ParseOptions withDiffStoragePolicy(DiffStoragePolicy diffStoragePolicy) {
        return new ParseOptions(diffStoragePolicy, this.annotationParser);
    }

    public static final ParseOptions Default = new ParseOptions(
            DiffStoragePolicy.DO_NOT_REMEMBER,
            DiffNodeParser.Default
    );
}

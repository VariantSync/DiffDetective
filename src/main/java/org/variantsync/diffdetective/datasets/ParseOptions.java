package org.variantsync.diffdetective.datasets;

import org.variantsync.diffdetective.feature.CPPAnnotationParser;

/**
 * Parse options that should be used when parsing commits and patches within a commit history.
 * @param diffStoragePolicy Decides if and how unix diffs should be remembered when parsing commits.
 * @param annotationParser A parser for parsing c preprocessor annotations.
 * @author Paul Bittner
 */
public record ParseOptions(DiffStoragePolicy diffStoragePolicy, CPPAnnotationParser annotationParser) {
    public enum DiffStoragePolicy {
        REMEMBER_DIFF,
        REMEMBER_STRIPPED_DIFF,
        REMEMBER_FULL_DIFF,
        DO_NOT_REMEMBER,
    }

    /**
     * Creates ParseOptions with the default value for {@link DiffStoragePolicy}.
     * @see ParseOptions#Default
     * @param annotationParser A parser for parsing c preprocessor annotations.
     */
    public ParseOptions(CPPAnnotationParser annotationParser) {
        this(Default.diffStoragePolicy, annotationParser);
    }

    /**
     * Creates ParseOptions with the given policy for storing diffs.
     * @see ParseOptions#ParseOptions(DiffStoragePolicy, CPPAnnotationParser)
     * @param diffStoragePolicy Decides if and how unix diffs should be remembered when parsing commits.
     */
    public ParseOptions withDiffStoragePolicy(DiffStoragePolicy diffStoragePolicy) {
        return new ParseOptions(diffStoragePolicy, this.annotationParser);
    }

    /**
     * Default value for ParseOptions that does not remember parsed unix diffs
     * and uses the default value for the parsing annotations ({@link CPPAnnotationParser#Default}).
     */
    public static final ParseOptions Default = new ParseOptions(
            DiffStoragePolicy.DO_NOT_REMEMBER,
            CPPAnnotationParser.Default
    );
}

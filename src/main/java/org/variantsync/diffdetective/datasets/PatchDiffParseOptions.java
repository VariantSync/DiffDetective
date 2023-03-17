package org.variantsync.diffdetective.datasets;

import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;

/**
 * Parse options that should be used when parsing commits and patches within a commit history.
 * @param diffStoragePolicy Decides if and how unix diffs should be remembered in a parsed
 *                          {@link org.variantsync.diffdetective.diff.git.PatchDiff} when parsing commits.
 * @param diffTreeParseOptions Options for parsing a patch to a {@link org.variantsync.diffdetective.variation.diff.DiffTree}.
 *                             For more information, see {@link DiffTreeParseOptions}.
 * @author Paul Bittner
 */
public record PatchDiffParseOptions(
        DiffStoragePolicy diffStoragePolicy,
        DiffTreeParseOptions diffTreeParseOptions
) {
    public enum DiffStoragePolicy {
        REMEMBER_DIFF,
        REMEMBER_STRIPPED_DIFF,
        REMEMBER_FULL_DIFF,
        DO_NOT_REMEMBER,
    }

    /**
     * Creates PatchDiffParseOptions with the given annotation parser.
     */
    public PatchDiffParseOptions withAnnotationParser(CPPAnnotationParser annotationParser) {
        return new PatchDiffParseOptions(
                this.diffStoragePolicy(),
                this.diffTreeParseOptions().withAnnotationParser(annotationParser)
        );
    }

    /**
     * Creates PatchDiffParseOptions with the given policy for storing diffs.
     */
    public PatchDiffParseOptions withDiffStoragePolicy(DiffStoragePolicy diffStoragePolicy) {
        return new PatchDiffParseOptions(
                diffStoragePolicy,
                this.diffTreeParseOptions()
        );
    }

    /**
     * Default value for PatchDiffParseOptions that does not remember parsed unix diffs
     * and uses the default value for the parsing DiffTrees ({@link DiffTreeParseOptions#Default}).
     */
    public static final PatchDiffParseOptions Default = new PatchDiffParseOptions(
            DiffStoragePolicy.DO_NOT_REMEMBER,
            DiffTreeParseOptions.Default
    );
}

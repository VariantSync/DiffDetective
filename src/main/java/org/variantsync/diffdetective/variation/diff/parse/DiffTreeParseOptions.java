package org.variantsync.diffdetective.variation.diff.parse;

import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;

/**
 * Parse options that should be used when parsing {@link org.variantsync.diffdetective.variation.diff.DiffTree}s.
 * @param annotationParser A parser for parsing c preprocessor annotations.
 * @param collapseMultipleCodeLines Whether multiple consecutive code lines with the same diff
 * type should be collapsed into a single artifact node.
 * @param ignoreEmptyLines Whether to add {@code DiffNode}s for empty lines (regardless of their {@code DiffType}).
 *                         If {@link #collapseMultipleCodeLines} is {@code true} empty lines are also not added to
 *                         existing {@code DiffNode}s.
 * @author Paul Bittner
 */
public record DiffTreeParseOptions(
        CPPAnnotationParser annotationParser,
        boolean collapseMultipleCodeLines,
        boolean ignoreEmptyLines
) {
    /**
     * Creates DiffTreeParseOptions with the default parser as specified in {@link #Default}.
     */
    public DiffTreeParseOptions(
            boolean collapseMultipleCodeLines,
            boolean ignoreEmptyLines
    ) {
        this(
                Default.annotationParser(),
                collapseMultipleCodeLines,
                ignoreEmptyLines
        );
    }

    /**
     * Creates DiffTreeParseOptions with the given annotation parser.
     */
    public DiffTreeParseOptions withAnnotationParser(CPPAnnotationParser annotationParser) {
        return new DiffTreeParseOptions(
                annotationParser,
                this.collapseMultipleCodeLines(),
                this.ignoreEmptyLines()
        );
    }

    /**
     * Default value for DiffTreeParseOptions that does not remember parsed unix diffs
     * and uses the default value for the parsing annotations ({@link CPPAnnotationParser#Default}).
     */
    public static final DiffTreeParseOptions Default = new DiffTreeParseOptions(
            CPPAnnotationParser.Default,
            true,
            true
    );
}

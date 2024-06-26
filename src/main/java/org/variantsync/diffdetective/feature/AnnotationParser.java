package org.variantsync.diffdetective.feature;

import org.prop4j.Node;
import org.variantsync.diffdetective.error.UnparseableFormulaException;

/**
 * Interface for a parser that analyzes annotations in parsed text. The parser is responsible for determining the type
 * of the annotation (see {@link AnnotationType}), and parsing the annotation into a {@link Node}.
 * <p>
 * See {@link PreprocessorAnnotationParser} for an example of how an implementation of AnnotationParser could look like.
 * </p>
 */
public interface AnnotationParser {
    /**
     * Determine the annotation type for the given piece of text (typically a line of source code).
     *
     * @param text The text of which the type is determined.
     * @return The annotation type of the piece of text.
     */
    AnnotationType determineAnnotationType(String text);

    /**
     * Parse the condition of the given text containing an annotation (typically a line of source code).
     *
     * @param text The text containing a conditional annotation
     * @return The formula of the condition in the given annotation.
     * If no such formula could be extracted, returns a Literal with the line's condition as name.
     * @throws UnparseableFormulaException if there is an error while parsing.
     */
    Node parseAnnotation(String text) throws UnparseableFormulaException;
}

package org.variantsync.diffdetective.feature;

/**
 * AnnotationType is an enum that describes whether a piece of text marks the start of an
 * annotation, the end of an annotation, or no annotation at all.
 */
public enum AnnotationType {
    /**
     * The piece of text (e.g., "#if ...") contains a conditional annotation that starts a new
     * annotated subtree in the variation tree.
     */
    If("if"),

    /**
     * The piece of text (e.g., "#elif ...") contains a conditional annotation which is only checked
     * if the conditions of all preceding annotations belonging to the same annotation chain are not fulfilled.
     */
    Elif("elif"),

    /**
     * The piece of text (e.g., "#else") contains an annotation that marks a subtree as used alternative
     * if the condition of the preceding annotation in the same annotation chain is not fulfilled.
     */
    Else("else"),

    /**
     * The piece of text (e.g., "#endif") marks the end of an annotation (chain).
     */
    Endif("endif"),

    /**
     * The piece of text contains no annotation. This usually means that it contains an artifact.
     */
    None("NONE");

    public final String name;

    AnnotationType(String name) {
        this.name = name;
    }

    /**
     * Creates a NodeType from its value names.
     *
     * @param name a string that equals the name of one value of this enum (ignoring case)
     * @return The NodeType that has the given name
     * @see Enum#name()
     */
    public static AnnotationType fromName(final String name) {
        for (AnnotationType candidate : values()) {
            if (candidate.toString().equalsIgnoreCase(name)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Given string \"" + name + "\" is not the name of an AnnotationName.");
    }
}

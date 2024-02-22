package org.variantsync.diffdetective.variation;

import org.variantsync.diffdetective.feature.AnnotationType;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.tree.VariationNode; // For Javadoc

/**
 * The type of nodes of a {@link DiffNode} and a {@link VariationNode}.
 * Corresponds to the tau function from our paper.
 */
public enum NodeType {
    // Mapping types
    IF("if"),
    ELSE("else"),
    ELIF("elif"),

    // Artifact types
    ARTIFACT("artifact");

    public final String name;

    NodeType(String name) {
        this.name = name;
    }

    /**
     * Returns true iff this node type represents a conditional feature annotation
     * (i.e., if or elif).
     */
    public boolean isConditionalAnnotation() {
        return this == IF || this == ELIF;
    }

    /**
     * Returns true iff this node type represents a feature mapping.
     */
    public boolean isAnnotation() {
        return this != ARTIFACT;
    }

    /**
     * Creates a NodeType from its value names.
     *
     * @param name a string that equals the name of one value of this enum (ignoring
     *             case)
     * @return The NodeType that has the given name
     * @see Enum#name()
     */
    public static NodeType fromName(final String name) {
        for (NodeType candidate : values()) {
            if (candidate.toString().equalsIgnoreCase(name)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Given string \"" + name + "\" is not the name of a NodeType.");
    }

    /**
     * Creates a NodeType from an AnnotationType.
     * <p>
     * All AnnotationType variants except for 'Endif' are supported.
     * There is no valid representation for 'Endif' annotations. Thus, the method throws an IllegalArgumentException
     * if it is given an 'Endif'.
     * </p>
     *
     * @param annotationType a variant of AnnotationType
     * @return The NodeType that fits the given AnnotationType
     */
    public static NodeType fromAnnotationType(final AnnotationType annotationType) {
        return switch (annotationType) {
            case If -> NodeType.IF;
            case Elif -> NodeType.ELIF;
            case Else -> NodeType.ELSE;
            case None -> NodeType.ARTIFACT;
            case Endif -> throw new IllegalArgumentException(annotationType + "has no NodeType counterpart");
        };
    }

    /**
     * Returns the number of bits required for storing.
     */
    public static int getRequiredBitCount() {
        return 3;
    }
}

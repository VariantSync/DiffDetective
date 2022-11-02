package org.variantsync.diffdetective.diff.difftree;

/**
 * The type of nodes in a {@link DiffTree}.
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
     * Returns true iff this node type represents a conditional feature annotation (i.e., if or elif).
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
     * @see Enum#name()
     * @param name a string that equals the name of one value of this enum (ignoring case)
     * @return The NodeType that has the given name
     */
    public static NodeType fromName(final String name) {
        for (NodeType candidate : values()) {
            if (candidate.toString().equalsIgnoreCase(name)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Given string \"" + name + "\" is not the name of a NodeType.");
    }
}

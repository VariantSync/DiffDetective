package org.variantsync.diffdetective.variation.tree;

import org.variantsync.diffdetective.variation.diff.NodeType;

/**
 * Mixin providing convenience methods for checking certain node types.
 *
 * @author Benjamin Moosherr
 */
public interface HasNodeType {
    /** Returns the type of this node. */
    public NodeType getNodeType();

    /** Returns {@code true} iff this node is an {@link NodeType#ARTIFACT artifact}. */
    public default boolean isArtifact() {
        return getNodeType().equals(NodeType.ARTIFACT);
    }

    /** Returns {@code true} iff this node is an {@link NodeType#IF if}. */
    public default boolean isIf() {
        return getNodeType().equals(NodeType.IF);
    }

    /** Returns {@code true} iff this node is an {@link NodeType#ELIF elif}. */
    public default boolean isElif() {
        return getNodeType().equals(NodeType.ELIF);
    }

    /** Returns {@code true} iff this node is an {@link NodeType#ELSE else}. */
    public default boolean isElse() {
        return getNodeType().equals(NodeType.ELSE);
    }

    /** Returns {@code true} iff this node is an {@link NodeType#isAnnotation() annotation}. */
    public default boolean isAnnotation() {
        return getNodeType().isAnnotation();
    }

    /**
     * Returns {@code true} iff this node is a {@link NodeType#isConditionalAnnotation()
     * conditional annotation}.
     */
    public default boolean isConditionalAnnotation() {
        return getNodeType().isConditionalAnnotation();
    }
}

package org.variantsync.diffdetective.variation.tree;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.LineRange;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode; // For Javdoc
import org.variantsync.diffdetective.variation.diff.VariationDiff; // For Javdoc
import org.variantsync.diffdetective.variation.diff.DiffType;

import java.util.*;

/**
 * A single node in a variation tree.
 *
 * <p>A variation tree is a representation of source code in a software product line. Each node in
 * such a tree is either an {@link isArtifact artifact} or a {@link isAnnotation mapping node}.
 * Artifacts represent source code which may be reused in multiple variants. In contrast, mappings
 * do not contain source code of a specific variant. They store information about which variants
 * contain the artifacts it has as {@link getChildren children} in the form of a
 * {@link getFeatureMapping feature mapping}.
 *
 * <p>See definition 2.2 of
 * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
 * our ESEC/FSE'22 paper</a> for a mathematical formalization or variation trees. In contrast to
 * this formalization, this implementation optimizes for variation trees obtained from source code
 * annotated with the C preprocessor syntax. This mostly means that there is the additional
 * {@link NodeType#ELIF} node type.
 *
 * <p>This class contains references to all of its children and its parent so all connected nodes of
 * a variation tree can be reached through each node of the variation tree. Nevertheless, most of
 * the time only node itself or its subtree (the reflexive hull of {@link getChildren}) is meant
 * when when referencing a {@code VariationTreeNode}. Use {@link VariationTree} to unambiguously
 * refer to a whole variation tree.
 *
 * <p>If possible, algorithms should be using {@link VariationNode} instead of this concrete
 * implementation. This allows the usage of a projection of a {@link DiffNode} instead of a concrete
 * variation node.
 *
 * <p>To compare two variation trees use {@link VariationDiff} which uses the aforementioned
 * {@link DiffNode}s.
 *
 * @see VariationTree
 * @see VariationDiff
 * @see DiffNode
 * @author Benjamin Moosherr
 */
public class ConcreteTreeNode<L extends Label> extends TreeNode<ConcreteTreeNode<L>, L> {
    /**
     * The label together with the node type of this node, which determines the type of the
     * represented element in the diff (e.g., mapping or artifact).
     */
    private L label;

    /**
     * The parent of this node. It's {@code null} iff this node doesn't have a parent.
     *
     * <p>Invariant: Iff {@code parent != null} then {@code parent.childOrder.contains(this)}.
     */
    private ConcreteTreeNode<L> parent;

    /**
     * The list for maintaining the order of all children of this node.
     *
     * @see parent
     */
    private final List<ConcreteTreeNode<L>> childOrder;

    /**
     * Creates a new node of a variation tree.
     *
     * The newly created node is not connected to any other nodes.
     *
     * The new node takes ownership of the given label without copying it. Beware of any further
     * modifications to this list.
     *
     * @param nodeType the type of this node
     * @param featureMapping the direct feature mapping of this node, has be non null iff
     * {@code nodeType.isConditionalAnnotation} is {@code true}
     * @param lineRange the line range of the code of {@code label}
     * @param label a list of lines used as label
     * @see addChild
     * @see addBelow
     */
    public ConcreteTreeNode(L label) {
        super();

        this.label = label;
        this.childOrder = new ArrayList<>();
    }

    @Override
    public ConcreteTreeNode<L> upCast() {
        return this;
    }

    @Override
    public L getLabel() {
        return label;
    }

    /**
     * Replaces the current label by {@code newLabelLines}.
     *
     * @see getLabel
     */
    public void setLabel(L newLabel) {
        label = newLabel;
    }

    @Override
    public ConcreteTreeNode<L> getParent() {
        return parent;
    }

    /**
     * Sets {@code newParent} as the new parent of this node.
     *
     * <p>This node must not have a parent yet.
     */
    private void setParent(final ConcreteTreeNode<L> newParent) {
        Assert.assertTrue(parent == null);
        this.parent = newParent;
    }

    @Override
    public List<ConcreteTreeNode<L>> getChildren() {
        return Collections.unmodifiableList(childOrder);
    }

    @Override
    public void addChild(final ConcreteTreeNode<L> child) {
        child.setParent(this);
        childOrder.add(child);
    }

    @Override
    public void insertChild(final ConcreteTreeNode<L> child, int index) {
        child.setParent(this);
        childOrder.add(index, child);
    }

    @Override
    public void removeChild(final ConcreteTreeNode<L> child) {
        Assert.assertTrue(isChild(child));
        child.parent = null;
        childOrder.remove(child);
    }

    @Override
    public void removeAllChildren() {
        for (var child : childOrder) {
            child.parent = null;
        }

        childOrder.clear();
    }

    /**
     * Returns an integer that uniquely identifiers this node within its variation tree.
     *
     * <p>From the returned id a new node with all essential attributes ({@link getNodeType node
     * type} and {@link getLineRange start line number}) can be reconstructed by using
     * {@link fromID}.
     *
     * <p>Note that this encoding assumes that line numbers fit into {@code 26} bits.
     */
    @Override
    public int getID() {
        // Add one to ensure invalid (negative) line numbers don't cause issues.
        final int lineNumber = 1 + getLineRange().fromInclusive();

        final int usedBitCount = DiffType.getRequiredBitCount() + NodeType.getRequiredBitCount();
        Assert.assertTrue((lineNumber << usedBitCount) >> usedBitCount == lineNumber);

        int id;
        id = lineNumber;

        // This makes `VariationTreeNode.toID` compatible with `DiffNode.toID`
        id <<= DiffType.getRequiredBitCount();
        id |= DiffType.NON.ordinal();

        id <<= NodeType.getRequiredBitCount();
        id |= getNodeType().ordinal();
        return id;
    }

    /**
     * Reconstructs a node from the given {@link getID id} and label.
     * The almost-inverse function is {@link getID()} but the conversion is not lossless.
     *
     * @param id the id from which to reconstruct the node
     * @param label the label the node should have
     * @return the reconstructed node
     */
    public static <L extends Label> ConcreteTreeNode<L> fromID(int id, L label) {
        final int nodeTypeBitmask = (1 << NodeType.getRequiredBitCount()) - 1;
        final int nodeTypeOrdinal = id & nodeTypeBitmask;
        id >>= NodeType.getRequiredBitCount();

        final int diffTypeBitmask = (1 << DiffType.getRequiredBitCount()) - 1;
        Assert.assertEquals(DiffType.NON.ordinal(), id & diffTypeBitmask);
        id >>= DiffType.getRequiredBitCount();

        final int from = id - 1;

        var nodeType = NodeType.values()[nodeTypeOrdinal];
        return new ConcreteTreeNode<L>(
                nodeType,
                nodeType.isConditionalAnnotation() ? FixTrueFalse.True : null,
                LineRange.SingleLine(from),
                label
        );
    }

    @Override
    public String toString() {
        return isRoot() ? "ROOT" : getLabel().toString();
    }
}

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
public class VariationTreeNode<L extends Label> extends VariationNode<VariationTreeNode<L>, L> {
    /**
     * The label together with the node type of this node, which determines the type of the
     * represented element in the diff (e.g., mapping or artifact).
     */
    private VariationLabel<L> label;

    /**
     * The range of line numbers of this node's corresponding source code.
     */
    private LineRange lineRange;

    /**
     * The direct feature mapping of this node.
     *
     * <p>This is {@code null} iff {@link isConditionalAnnotation} is {@code false}.
     */
    private Node featureMapping;

    /**
     * The parent of this node. It's {@code null} iff this node doesn't have a parent.
     *
     * <p>Invariant: Iff {@code parent != null} then {@code parent.childOrder.contains(this)}.
     */
    private VariationTreeNode<L> parent;

    /**
     * The list for maintaining the order of all children of this node.
     *
     * @see parent
     */
    private final List<VariationTreeNode<L>> childOrder;

    /**
     * The line with the endif of the corresponding if, if the node is an if node, otherwise null
     */
    private List<String> endIf = null;

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
    public VariationTreeNode(
        NodeType nodeType,
        Node featureMapping,
        LineRange lineRange,
        L label
    ) {
        super();

        this.label = new VariationLabel<>(nodeType, label);
        this.lineRange = lineRange;
        this.featureMapping = featureMapping;

        this.childOrder = new ArrayList<>();
    }

    /**
     * Convenience constructor for creating the root of a {@link VariationTree}.
     *
     * <p>The newly created root has no children yet.
     *
     * <p>The root is a neutral annotation (i.e., its type if {@link NodeType#IF} and its feature
     * mapping is {@code "true"}). The label of this node is empty and therefore the line numbers
     * are {@link DiffLineNumber#InvalidLineNumber invalid}.
     *
     * @see addChild
     */
    public static <L extends Label> VariationTreeNode<L> createRoot(L label) {
        return new VariationTreeNode<>(
                NodeType.IF,
                FixTrueFalse.True,
                LineRange.Invalid(),
                label
        );
    }

    /**
     * Convenience constructor for creating an artifact node of a {@link VariationTree}.
     *
     * @param lineRange the line range of the code of {@code label}
     * @param label a list of lines used as label
     * @see addBelow
     */
    public static <L extends Label> VariationTreeNode<L> createArtifact(LineRange lineRange, L label) {
        return new VariationTreeNode<>(NodeType.ARTIFACT, null, lineRange, label);
    }

    @Override
    public VariationTreeNode<L> upCast() {
        return this;
    }

    @Override
    public NodeType getNodeType() {
        return label.getNodeType();
    }

    @Override
    public L getLabel() {
        return label.getInnerLabel();
    }

    /**
     * Replaces the current label by {@code newLabelLines}.
     *
     * @see getLabel
     */
    public void setLabel(L newLabel) {
        label.setInnerLabel(newLabel);
    }

    @Override
    public LineRange getLineRange() {
        return lineRange;
    }

    @Override
    public void setLineRange(LineRange lineRange) {
        this.lineRange = lineRange;
    }

    @Override
    public VariationTreeNode<L> getParent() {
        return parent;
    }

    /**
     * Sets {@code newParent} as the new parent of this node.
     *
     * <p>This node must not have a parent yet.
     */
    private void setParent(final VariationTreeNode<L> newParent) {
        Assert.assertTrue(parent == null);
        this.parent = newParent;
    }

    @Override
    public List<VariationTreeNode<L>> getChildren() {
        return Collections.unmodifiableList(childOrder);
    }

    @Override
    public void addChild(final VariationTreeNode<L> child) {
        child.setParent(this);
        childOrder.add(child);
    }

    @Override
    public void insertChild(final VariationTreeNode<L> child, int index) {
        child.setParent(this);
        childOrder.add(index, child);
    }

    @Override
    public void removeChild(final VariationTreeNode<L> child) {
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
     * Returns the line with the endif of the corresponding if, if the node is an if node, otherwise null
     * @return String, the Line with endif
     */
    public List<String> getEndIf() {
        return endIf;
    }

    /**
     * Sets the line with the endif of the corresponding if, if the node is an if node
     * @param endIf String, the Line with endif
     */
    public void setEndIf(List<String> endIf) {
        this.endIf = endIf;
    }

    @Override
    public Node getFormula() {
        return featureMapping;
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
    public static <L extends Label> VariationTreeNode<L> fromID(int id, L label) {
        final int nodeTypeBitmask = (1 << NodeType.getRequiredBitCount()) - 1;
        final int nodeTypeOrdinal = id & nodeTypeBitmask;
        id >>= NodeType.getRequiredBitCount();

        final int diffTypeBitmask = (1 << DiffType.getRequiredBitCount()) - 1;
        Assert.assertEquals(DiffType.NON.ordinal(), id & diffTypeBitmask);
        id >>= DiffType.getRequiredBitCount();

        final int from = id - 1;

        var nodeType = NodeType.values()[nodeTypeOrdinal];
        return new VariationTreeNode<L>(
                nodeType,
                nodeType.isConditionalAnnotation() ? FixTrueFalse.True : null,
                LineRange.SingleLine(from),
                label
        );
    }

    /**
     * Creates a deep copy of this node.
     */
    public VariationTreeNode<L> deepCopy() {
        return toVariationTree();
    }

    /**
     * Creates a deep copy of this node.
     *
     * <p>The map {@code oldToNew} should be empty as it will be filled by this method. After the
     * method call, the map keys will contain all nodes in this node's subtree (including this
     * node). The corresponding values will be the nodes in the returned node's subtree (including
     * the returned node), where each pair (k, v) denotes that v was cloned from k.
     *
     * @param oldToNew A map that memorizes the translation of individual nodes.
     * @return A deep copy of this tree.
     */
    public VariationTreeNode<L> deepCopy(final Map<VariationTreeNode<L>, VariationTreeNode<L>> oldToNew) {
        return toVariationTree(oldToNew);
    }

    @Override
    public String toString() {
        String s;
        if (isArtifact()) {
            s = String.format("%s in the lines %s", getNodeType(), lineRange);
        } else if (isRoot()) {
            s = "ROOT";
        } else {
            s = String.format("%s in the lines %s with \"%s\"", getNodeType(),
                    lineRange, featureMapping);
        }
        return s;
    }
}

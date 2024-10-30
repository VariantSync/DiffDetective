package org.variantsync.diffdetective.variation.diff;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.LineRange;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.tree.HasNodeType;
import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.functjonal.Cast;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Implementation of a node in a {@link VariationDiff}.
 * A DiffNode represents a single node within a variation tree diff (according to our ESEC/FSE'22 paper), but is specialized
 * to the target domain of preprocessor-based software product lines.
 * Thus, opposed to the generic mathematical model of variation tree diffs, a DiffNode always stores lines of text, line numbers, and child ordering information as its label.
 * Each DiffNode may be edited according to its {@link DiffType} and represents a source code element according to its {@link NodeType}.
 * DiffNode's store parent and child information to build a graph.
 *
 * @param <L> The type of label stored in this node.
 *
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr
 */
public class DiffNode<L extends Label> implements HasNodeType {
    /**
     * The diff type of this node, which determines if this node represents
     * an inserted, removed, or unchanged element in a diff.
     */
    public DiffType diffType;

    /**
     * The label together with the node type of this node, which determines the type of the
     * represented element in the diff (e.g., mapping or artifact).
     */
    public final VariationLabel<L> label;

    private DiffLineNumber from = DiffLineNumber.Invalid();
    private DiffLineNumber to = DiffLineNumber.Invalid();

    private Node featureMapping;

    private List<String> endIf = null;

    /**
     * The parents {@link DiffNode} before and after the edit.
     * This array has to be indexed by {@code Time.ordinal()}
     *
     * Invariant: Iff {@code getParent(time) != null} then
     * {@code getParent(time).getChildOrder(time).contains(this)}.
     */
    private DiffNode<L>[] parents = Cast.unchecked(Array.newInstance(DiffNode.class, 2));

    /**
     * The children before and after the edit.
     * This array has to be indexed by {@code Time.ordinal()}
     *
     * Invariant: Iff {@code getChildOrder(time).contains(child)} then
     * {@code child.getParent(time) == this}.
     */
    private final List<DiffNode<L>>[] children = Cast.unchecked(Array.newInstance(List.class, 2));

    /**
     * Cache for before and after projections.
     * It stores the projection node at each time so that only one instance of {@link Projection}
     * per {@link Time} is ever created. This array has to be indexed by {@code Time.ordinal()}
     *
     * <p>This field is required to allow identity tests of {@link Projection}s with {@code ==}.
     */
    private Projection<L>[] projections = Cast.unchecked(Array.newInstance(Projection.class, 2));

    /**
     * Creates a DiffNode with the given parameters.
     * @param diffType The type of change made to this node.
     * @param nodeType The type of this node (i.e., mapping or artifact).
     * @param fromLines The starting line number of the corresponding text.
     * @param toLines The ending line number of the corresponding text.
     * @param featureMapping The formula stored in this node. Should be null for artifact nodes.
     * @param label The label of this node.
     */
    public DiffNode(DiffType diffType, NodeType nodeType,
                    DiffLineNumber fromLines, DiffLineNumber toLines,
                    Node featureMapping, L label) {
        children[BEFORE.ordinal()] = new ArrayList<>();
        children[AFTER.ordinal()] = new ArrayList<>();

        this.diffType = diffType;
        this.label = new VariationLabel<>(nodeType, label);
        this.from = fromLines;
        this.to = toLines;
        this.featureMapping = featureMapping;
    }

    /**
     * Creates a new root node.
     * The root is a neutral annotation (i.e., its feature mapping is "true").
     */
    public static <L extends Label> DiffNode<L> createRoot(L label) {
        return new DiffNode<>(
                DiffType.NON,
                NodeType.IF,
                DiffLineNumber.Invalid(),
                DiffLineNumber.Invalid(),
                FixTrueFalse.True,
                label
        );
    }

    /**
     * Creates an artifact node with the given parameters.
     * For parameter descriptions, see {@link DiffNode#DiffNode(DiffType, NodeType, DiffLineNumber, DiffLineNumber, Node, L)}.
     * The <code>code</code> parameter will be set as the node's label by splitting it into lines.
     */
    public static DiffNode<DiffLinesLabel> createArtifact(DiffType diffType, DiffLineNumber fromLines, DiffLineNumber toLines, String code) {
        return new DiffNode<>(diffType, NodeType.ARTIFACT, fromLines, toLines, null, DiffLinesLabel.ofCodeBlock(code));
    }

    /**
     * The same as {@link DiffNode#createArtifact(DiffType, DiffLineNumber, DiffLineNumber, String)} but with a generic label.
     */
    public static <L extends Label> DiffNode<L> createArtifact(DiffType diffType, DiffLineNumber fromLines, DiffLineNumber toLines, L label) {
        return new DiffNode<L>(diffType, NodeType.ARTIFACT, fromLines, toLines, null, label);
    }

    /**
     * Returns the lines in the diff that are represented by this DiffNode as a single text.
     */
    public L getLabel() {
        return label.getInnerLabel();
    }

    /**
     * Sets the lines in the diff that are represented by this DiffNode to the given code.
     * Lines are identified by linebreak characters.
     */
    public void setLabel(L newLabel) {
        label.setInnerLabel(newLabel);
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

    /**
     * Gets the first {@code if} node in the path from the root to this node at the time
     * {@code time}.
     * @return The first {@code if} node in the path to the root at the time {@code time}
     */
    public DiffNode<L> getIfNode(Time time) {
        return projection(time).getIfNode().getBackingNode();
    }

    /**
     * Gets the length of the path from the root to this node at the time {@code time}.
     * @return the depth of the this node in the diff tree at the time {@code time}
     */
    public int getDepth(Time time) {
        return projection(time).getDepth();
    }

    /**
     * Returns true iff the path's in parent direction following the before parent and after parent
     * are the very same.
     */
    public boolean beforePathEqualsAfterPath() {
        if (getParent(BEFORE) == getParent(AFTER)) {
            if (getParent(BEFORE) == null) {
                // root
                return true;
            }

            return getParent(BEFORE).beforePathEqualsAfterPath();
        }

        return false;
    }

    /**
     * Returns the number of unique child nodes.
     */
    public int getTotalNumberOfChildren() {
        return (int) getAllChildrenStream().count();
    }

    /**
     * Gets the amount of nodes on the path from the root to this node which only exist at the time
     * {@code time}.
     */
    public int getChangeAmount(Time time) {
        if (isRoot()) {
            return 0;
        }

        var changeType = DiffType.thatExistsOnlyAt(time);

        if (isIf() && diffType.equals(changeType)) {
            return getParent(time).getChangeAmount(time) + 1;
        }

        if ((isElif() || isElse()) && diffType.equals(changeType)) {
            // if this is a removed elif or else we do not want to count the other branches of
            // this annotation
            // we thus go up the tree until we get the next if and continue with the parent of it
            return getParent(time).getIfNode(time).getParent(time).getChangeAmount(time) + 1;
        }

        return getParent(time).getChangeAmount(time);
    }

    /**
     * Adds this subtree below the given parents.
     * Inverse of drop.
     * @param newBeforeParent Node that should be this node's before parent. May be null.
     * @param newAfterParent Node that should be this node's after parent. May be null.
     */
    public void addBelow(final DiffNode<L> newBeforeParent, final DiffNode<L> newAfterParent) {
        if (getDiffType().existsAtTime(BEFORE) && newBeforeParent != null) {
            newBeforeParent.addChild(this, BEFORE);
        }
        if (getDiffType().existsAtTime(AFTER) && newAfterParent != null) {
            newAfterParent.addChild(this, AFTER);
        }
    }

    /**
     * Removes this subtree from its parents.
     * Inverse of addBelow.
     */
    public void drop() {
        Time.forAll(time -> {
            if (getParent(time) != null) {
                drop(time);
            }
        });
    }

    /**
     * Removes this subtree from its parents at the time {@code time}.
     */
    public void drop(Time time) {
        Assert.assertTrue(getParent(time) != null);

        getParent(time).removeChild(this, time);
    }

    /**
     * Returns the index of the given child in the list of children of this node.
     * Returns -1 if the given node is not a child of this node.
     */
    public int indexOfChild(final DiffNode<L> child, Time time) {
        return children[time.ordinal()].indexOf(child);
    }

    /**
     * Insert {@code child} as child at the time {@code time} at the position {@code index}.
     */
    public void insertChild(final DiffNode<L> child, int index, Time time) {
        Assert.assertTrue(child.getDiffType().existsAtTime(time));
        Assert.assertFalse(isChild(child, time), () ->
            "Given child " + child + " already has a " + time + " parent (" + child.getParent(time) + ")!");

        children[time.ordinal()].add(index, child);
        child.parents[time.ordinal()] = this;
    }

    /**
     * The same as {@link DiffNode#insertChild} but puts the node at the end of the children
     * list instead of inserting it at a specific index.
     */
    public void addChild(final DiffNode<L> child, Time time) {
        Assert.assertTrue(child.getDiffType().existsAtTime(time));
        Assert.assertFalse(isChild(child, time), () ->
            "Given child " + child + " already has a " + time + " parent (" + child.getParent(time) + ")!");

        children[time.ordinal()].add(child);
        child.parents[time.ordinal()] = this;
    }

    /**
     * Adds all given nodes at the time {@code time} as children using {@link DiffNode#addChild}.
     * @param children Nodes to add as children.
     * @param time whether to add {@code children} before or after the edit
     */
    public void addChildren(final Collection<DiffNode<L>> children, Time time) {
        for (final DiffNode<L> child : children) {
            addChild(child, time);
        }
    }

    /**
     * Removes the given node from this node's children before or after the edit.
     * The node might still remain a child after or before the edit.
     * @param child the child to remove
     * @param time whether {@code child} should be removed before or after the edit
     */
    public void removeChild(final DiffNode<L> child, Time time) {
        Assert.assertTrue(isChild(child, time));

        child.parents[time.ordinal()] = null;
        children[time.ordinal()].remove(child);
    }

    /**
     * Removes all given children for all times.
     * None of the given nodes will be a child, neither before nor after the edit, afterwards.
     * @param childrenToRemove Nodes that should not be children of this node anymore.
     */
    public void removeChildren(final Collection<DiffNode<L>> childrenToRemove) {
        for (final DiffNode<L> childToRemove : childrenToRemove) {
            Time.forAll(time -> {
                if (isChild(childToRemove, time)) {
                    removeChild(childToRemove, time);
                }
            });
        }
    }

    /**
     * Removes all children before or after the edit.
     * Afterwards, this node will have no children at the given time.
     * @param time whether to remove all children before or after the edit
     * @return All removed children.
     */
    public List<DiffNode<L>> removeChildren(Time time) {
        for (var child : children[time.ordinal()]) {
            child.parents[time.ordinal()] = null;
        }

        final List<DiffNode<L>> orphans = children[time.ordinal()];
        children[time.ordinal()] = new ArrayList<>();
        return orphans;
    }

    /**
     * Removes all children from the given node and adds them as children to this node at the respective times.
     * The order of children is not stable because first all before children are transferred and then all after children.
     * The given node will have no children afterwards.
     * @param other The node whose children should be stolen.
     */
    public void stealChildrenOf(final DiffNode<L> other) {
        Time.forAll(time -> addChildren(other.removeChildren(time), time));
    }

    /**
     * Returns the parent of this node before or after the edit.
     */
    public DiffNode<L> getParent(Time time) {
        return parents[time.ordinal()];
    }

    /**
     * Returns the starting line number of this node's corresponding text block.
     */
    public DiffLineNumber getFromLine() {
        return from;
    }

    public void setFromLine(DiffLineNumber from) {
        this.from = from.as(diffType);
    }

    /**
     * Returns the end line number of this node's corresponding text block.
     * The line number is exclusive (i.e., it points 1 behind the last included line).
     */
    public DiffLineNumber getToLine() {
        return to;
    }

    public void setToLine(DiffLineNumber to) {
        this.to = to.as(diffType);
    }

    /**
     * Returns the range of line numbers of this node's corresponding source code in the text-based diff.
     * @see DiffLineNumber#rangeInDiff
     */
    public LineRange getLinesInDiff() {
        return DiffLineNumber.rangeInDiff(from, to);
    }

    /**
     * Returns the range of line numbers of this node's corresponding source code before or after
     * the edit.
     */
    public LineRange getLinesAtTime(Time time) {
        return DiffLineNumber.rangeAtTime(from, to, time);
    }

    /**
     * Returns the range of line numbers of this node's corresponding source code before or after
     * the edit.
     */
    public void setLinesAtTime(LineRange lineRange, Time time) {
        from = from.withLineNumberAtTime(lineRange.fromInclusive(), time);
        to = to.withLineNumberAtTime(lineRange.toExclusive(), time);
    }

    /**
     * Returns the formula that is stored in this node.
     * The formula is null for artifact nodes (i.e., {@link NodeType#ARTIFACT}).
     * The formula is not null for mapping nodes
     * @see NodeType#isAnnotation
     */
    public Node getFormula() {
        return featureMapping;
    }

    public void setFormula(Node featureMapping) {
        Assert.assertTrue(
                (featureMapping != null) == this.isConditionalAnnotation(),
                () -> {
                    String s = "Given formula " + featureMapping;
                    if (this.isConditionalAnnotation()) {
                        return s + " should not be null!";
                    }
                    return s + " must be null but is not!";
                }
        );

        this.featureMapping = featureMapping;
    }

    /**
     * Returns the order of the children at {@code time}.
     */
    public List<DiffNode<L>> getChildOrder(Time time) {
        return Collections.unmodifiableList(children[time.ordinal()]);
    }

    /**
     * Returns an efficient stream representation of all direct children without duplicates.
     * In particular, children which are both before and after children of this node are only
     * contained once. The order of the children is unspecified.
     */
    public Stream<DiffNode<L>> getAllChildrenStream() {
        return Stream.concat(
            children[BEFORE.ordinal()].stream(),
            children[AFTER.ordinal()].stream().filter(child -> child.getParent(BEFORE) != this)
        );
    };

    /**
     * Returns an efficient iterable representation of all direct children without duplicates.
     * Note: The returned iterable can only be traversed once.
     * @see getAllChildrenStream
     */
    public Iterable<DiffNode<L>> getAllChildren() {
        return getAllChildrenStream()::iterator;
    }

    /**
     * Returns a new set with all children of this node without duplicates.
     * @see getAllChildren
     */
    public Set<DiffNode<L>> getAllChildrenSet() {
        var result = new HashSet<DiffNode<L>>();
        getAllChildrenStream().forEach(result::add);
        return result;
    }

    /**
     * Returns the full feature mapping formula of this node.
     * The feature mapping of an {@link NodeType#IF} node is its {@link DiffNode#getFormula() direct feature mapping}.
     * The feature mapping of {@link NodeType#ELSE} and {@link NodeType#ELIF} nodes is determined by all formulas in the respective if-elif-else chain.
     * The feature mapping of an {@link NodeType#ARTIFACT artifact} node is the feature mapping of its parent.
     * See Equation (1) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @param time Whether to return the feature mapping clauses before or after the edit.
     * @return The feature mapping of this node for the given parent edges.
     */
    public Node getFeatureMapping(Time time) {
        return projection(time).getFeatureMapping();
    }

    /**
     * Returns the presence condition of this node before or after the edit.
     * See Equation (2) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @param time Whether to return the presence condition before or after the edit.
     * @return The presence condition of this node for the given parent edges.
     */
    public Node getPresenceCondition(Time time) {
        return projection(time).getPresenceCondition();
    }

    /**
     * Returns true iff this node is the before or after parent of the given node.
     */
    public boolean isChild(DiffNode<L> child) {
        return isChild(child, BEFORE) || isChild(child, AFTER);
    }

    /**
     * Returns true iff this node is the parent of the given node at the given time.
     */
    public boolean isChild(DiffNode<L> child, Time time) {
        return child.getParent(time) == this;
    }

    /**
     * Returns true iff this node has no children.
     */
    public boolean isLeaf() {
        return children[BEFORE.ordinal()].isEmpty() && children[AFTER.ordinal()].isEmpty();
    }

    /**
     * Returns true iff this node represents a removed element.
     * @see DiffType#REM
     */
    public boolean isRem() {
        return this.diffType.equals(DiffType.REM);
    }

    /**
     * Returns true iff this node represents an unchanged element.
     * @see DiffType#NON
     */
    public boolean isNon() {
        return this.diffType.equals(DiffType.NON);
    }

    /**
     * Returns true iff this node represents an inserted element.
     * @see DiffType#ADD
     */
    public boolean isAdd() {
        return this.diffType.equals(DiffType.ADD);
    }

    /**
      * Returns the diff type of this node.
     */
    public DiffType getDiffType() {
        return this.diffType;
    }

    @Override
    public NodeType getNodeType() {
        return label.getNodeType();
    }

    /**
     * Returns true if this node is a root node (has no parents).
     */
    public boolean isRoot() {
        return getParent(BEFORE) == null && getParent(AFTER) == null;
    }

    /**
     * @return An integer that uniquely identifiers this DiffNode within its patch.
     *
     * From the returned id a new node with all essential attributes reconstructed can be obtained
     * by using {@link DiffNode#fromID}.
     *
     * Note that only {@code 26} bits of the line number are encoded, so if the line number is bigger than
     * {@code 2^26}, this id will no longer be unique.
     */
    public int getID() {
        // Add one to ensure invalid (negative) line numbers don't cause issues.
        final int lineNumber = 1 + from.inDiff();

        final int usedBitCount = DiffType.getRequiredBitCount() + NodeType.getRequiredBitCount();
        Assert.assertTrue((lineNumber << usedBitCount) >> usedBitCount == lineNumber);

        int id;
        id = lineNumber;

        id <<= DiffType.getRequiredBitCount();
        id |= diffType.ordinal();

        id <<= NodeType.getRequiredBitCount();
        id |= getNodeType().ordinal();
        return id;
    }

    /**
     * Reconstructs a node from the given id and sets the given label.
     * An id uniquely determines a node's {@link DiffNode#getNodeType}, {@link DiffNode#diffType}, and {@link DiffLineNumber#inDiff line number in the diff}.
     * The almost-inverse function is {@link DiffNode#getID()} but the conversion is not lossless.
     * @param id The id from which to reconstruct the node.
     * @param label The label the node should have.
     * @return The reconstructed DiffNode.
     */
    public static DiffNode<DiffLinesLabel> fromID(int id, String label) {
        final int nodeTypeBitmask = (1 << NodeType.getRequiredBitCount()) - 1;
        final int nodeTypeOrdinal = id & nodeTypeBitmask;
        id >>= NodeType.getRequiredBitCount();

        final int diffTypeBitmask = (1 << DiffType.getRequiredBitCount()) - 1;
        final int diffTypeOrdinal = id & diffTypeBitmask;
        id >>= DiffType.getRequiredBitCount();

        final int fromInDiff = id - 1;

        var nodeType = NodeType.values()[nodeTypeOrdinal];
        return new DiffNode<>(
                DiffType.values()[diffTypeOrdinal],
                nodeType,
                new DiffLineNumber(fromInDiff, DiffLineNumber.InvalidLineNumber, DiffLineNumber.InvalidLineNumber),
                DiffLineNumber.Invalid(),
                nodeType.isConditionalAnnotation() ? FixTrueFalse.True : null,
                DiffLinesLabel.ofCodeBlock(label)
        );
    }

    /**
     * Checks that the VariationDiff is in a valid state.
     * In particular, this method checks that all edges are well-formed (e.g., edges can be inconsistent because edges are double-linked).
     * This method also checks that a node with exactly one parent was edited, and that a node with exactly two parents was not edited.
     * @see Assert#assertTrue
     * @throws AssertionError when an inconsistency is detected.
     */
    public void assertConsistency() {
        // check consistency of children lists and edges
        for (final DiffNode<L> c : getAllChildren()) {
            Assert.assertTrue(isChild(c), () -> "Child " + c + " of " + this + " is neither a before nor an after child!");
            Time.forAll(time -> {
                if (c.getParent(time) != null) {
                    Assert.assertTrue(c.getParent(time).isChild(c, time), () -> "The parent " + time.toString().toLowerCase() + " the edit of " + c + " doesn't contain that node as child");
                }
            });
        }

        final DiffNode<L> pb = getParent(BEFORE);
        final DiffNode<L> pa = getParent(AFTER);

        Assert.assertTrue(pb == null || pb.getDiffType().existsAtTime(BEFORE));
        Assert.assertTrue(pa == null || pa.getDiffType().existsAtTime(AFTER));

        // a node with exactly one parent was edited
        if (pb == null && pa != null) {
            Assert.assertTrue(isAdd());
        }
        if (pb != null && pa == null) {
            Assert.assertTrue(isRem());
        }
        // a node with exactly two parents was not edited
        if (pb != null && pa != null) {
            Assert.assertTrue(isNon());

            // If the parents are the same node, then the parent also has
            // to be non-edited.
            if (pb == pa) {
                Assert.assertTrue(pb.isNon());
            }
        }

        // Else and Elif nodes have an If or Elif as parent.
        if (this.isElse() || this.isElif()) {
            Time.forAll(time -> {
                if (getParent(time) != null) {
                    Assert.assertTrue(getParent(time).isIf() || getParent(time).isElif(), time + " parent " + getParent(time) + " of " + this + " is neither IF nor ELIF!");
                }
            });
        }

        // Only if and elif nodes have a formula
        if (this.isIf() || this.isElif()) {
            Assert.assertTrue(this.getFormula() != null, "If or elif without feature mapping!");
        } else {
            Assert.assertTrue(this.getFormula() == null, "Node with type " + getNodeType() + " has a non null feature mapping");
        }
    }

    /**
     * Returns a view of this {@code DiffNode} as a variation node at the time {@code time}.
     *
     * <p>See the {@code project} function in section 3.1 of
     * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
     * our paper</a>.
     */
    public Projection<L> projection(Time time) {
        Assert.assertTrue(getDiffType().existsAtTime(time));

        if (projections[time.ordinal()] == null) {
            projections[time.ordinal()] = new Projection<>(this, time);
        }

        return projections[time.ordinal()];
    }

    /**
     * Transforms a {@code VariationNode} into a {@code DiffNode} by diffing {@code variationNode}
     * to itself. Acts on only the given node and does not perform recursive translations.
     */
    public static <T extends VariationNode<T, L>, L extends Label> DiffNode<L> unchangedFlat(VariationNode<T, L> variationNode) {
        int from = variationNode.getLineRange().fromInclusive();
        int to = variationNode.getLineRange().toExclusive();

        return new DiffNode<>(
                DiffType.NON,
                variationNode.getNodeType(),
                new DiffLineNumber(from, from, from),
                new DiffLineNumber(to, to, to),
                variationNode.getFormula(),
                variationNode.getLabel()
        );
    }

    /**
     * Transforms a {@code VariationNode} into a {@code DiffNode} by diffing {@code variationNode}
     * to itself. Recursively translates all children.
     *
     * This is the inverse of {@link projection} iff the original {@link DiffNode} wasn't modified
     * (all node had a {@link getDiffType diff type} of {@link DiffType#NON}).
     *
     * @param convert A function to translate single nodes (without their hierarchy).
     */
    public static <T extends VariationNode<T, L1>, L1 extends Label, L2 extends Label> DiffNode<L2> unchanged(
            final Function<? super T, DiffNode<L2>> convert,
            VariationNode<T, L1> variationNode) {

        var diffNode = convert.apply(variationNode.upCast());

        for (var variationChildNode : variationNode.getChildren()) {
            var diffChildNode = unchanged(convert, variationChildNode);
            Time.forAll(time -> diffNode.addChild(diffChildNode, time));
        }

        return diffNode;
    }

    public DiffNode<L> deepCopy() {
        return deepCopy(new HashMap<>());
    }

    public DiffNode<L> deepCopy(HashMap<DiffNode<L>, DiffNode<L>> oldToNew) {
        DiffNode<L> copy = oldToNew.get(this);
        if (copy == null) {
            copy = shallowCopy();

            final var copyFinal = copy;
            Time.forAll(time -> {
                for (var child : getChildOrder(time)) {
                    copyFinal.addChild(child.deepCopy(oldToNew), time);
                }
            });

            oldToNew.put(this, copy);
        }

        return copy;
    }

    public DiffNode<L> shallowCopy() {
        return new DiffNode<L>(
            getDiffType(),
            getNodeType(),
            getFromLine(),
            getToLine(),
            getFormula(),
            Cast.unchecked(label.clone())
        );
    }

    /**
     * Transforms a {@code VariationNode} into a {@code DiffNode} by diffing {@code variationNode}
     * to itself. Recursively translates all children.
     *
     * This is the inverse of {@link projection} iff the original {@link DiffNode} wasn't modified
     * (all node had a {@link getDiffType diff type} of {@link DiffType#NON}).
     */
    public static <T extends VariationNode<T, L>, L extends Label> DiffNode<L> unchanged(VariationNode<T, L> variationNode) {
        return unchanged(DiffNode::unchangedFlat, variationNode);
    }

    /**
     * Returns true if this subtree is exactly equal to {@code other}.
     * This check uses equality checks instead of identity.
     */
    public boolean isSameAs(DiffNode<L> other) {
        return isSameAs(this, other, new HashSet<>());
    }

    private static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b, Set<DiffNode<L>> visited) {
        if (!visited.add(a)) {
            return true;
        }

        if (!(
                a.getDiffType().equals(b.getDiffType()) &&
                a.getNodeType().equals(b.getNodeType()) &&
                a.getFromLine().equals(b.getFromLine()) &&
                a.getToLine().equals(b.getToLine()) &&
                (a.getFormula() == null ? b.getFormula() == null : a.getFormula().equals(b.getFormula())) &&
                a.getLabel().equals(b.getLabel())
        )) {
            return false;
        }

        Iterator<DiffNode<L>> aIt = a.getAllChildren().iterator();
        Iterator<DiffNode<L>> bIt = b.getAllChildren().iterator();
        while (aIt.hasNext() && bIt.hasNext()) {
            if (!isSameAs(aIt.next(), bIt.next(), visited)) {
                return false;
            }
        }

        return aIt.hasNext() == bIt.hasNext();
    }

    @Override
    public String toString() {
        String s;
        if (isArtifact()) {
            s = String.format("%s_%s from %d to %d", diffType, getNodeType(), from.inDiff(), to.inDiff());
        } else if (isRoot()) {
            s = "ROOT";
        } else {
            s = String.format("%s_%s from %d to %d with \"%s\"", diffType, getNodeType(),
                    from.inDiff(), to.inDiff(), featureMapping);
        }
        return s;
    }
}

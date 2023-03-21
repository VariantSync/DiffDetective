package org.variantsync.diffdetective.variation.diff;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.LineRange;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.tree.HasNodeType;
import org.variantsync.diffdetective.variation.tree.VariationNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Implementation of a node in a {@link DiffTree}.
 * A DiffNode represents a single node within a variation tree diff (according to our ESEC/FSE'22 paper), but is specialized
 * to the target domain of preprocessor-based software product lines.
 * Thus, opposed to the generic mathematical model of variation tree diffs, a DiffNode always stores lines of text, line numbers, and child ordering information as its label.
 * Each DiffNode may be edited according to its {@link DiffType} and represents a source code element according to its {@link NodeType}.
 * DiffNode's store parent and child information to build a graph.
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr
 */
public class DiffNode implements HasNodeType {
    /**
     * The diff type of this node, which determines if this node represents
     * an inserted, removed, or unchanged element in a diff.
     */
    public final DiffType diffType;

    /**
     * The node type of this node, which determines the type of the represented
     * element in the diff (e.g., mapping or artifact).
     */
    public final NodeType nodeType;

    private DiffLineNumber from = DiffLineNumber.Invalid();
    private DiffLineNumber to = DiffLineNumber.Invalid();

    private Node featureMapping;
    private List<String> lines;

    /**
     * The parents {@link DiffNode} before and after the edit.
     * This array has to be indexed by {@code Time.ordinal()}
     *
     * Invariant: Iff {@code getParent(time) != null} then
     * {@code getParent(time).getChildOrder(time).contains(this)}.
     */
    private DiffNode[] parents = new DiffNode[2];

    /**
     * The children before and after the edit.
     * This array has to be indexed by {@code Time.ordinal()}
     *
     * Invariant: Iff {@code getChildOrder(time).contains(child)} then
     * {@code child.getParent(time) == this}.
     */
    @SuppressWarnings("unchecked")
    private final List<DiffNode>[] children = new ArrayList[2];

    /**
     * Cache for before and after projections.
     * It stores the projection node at each time so that only one instance of {@link Projection}
     * per {@link Time} is ever created. This array has to be indexed by {@code Time.ordinal()}
     *
     * <p>This field is required to allow identity tests of {@link Projection}s with {@code ==}.
     */
    private Projection[] projections = new Projection[2];

    /**
     * Creates a DiffNode with the given parameters.
     * @param diffType The type of change made to this node.
     * @param nodeType The type of this node (i.e., mapping or artifact).
     * @param fromLines The starting line number of the corresponding text.
     * @param toLines The ending line number of the corresponding text.
     * @param featureMapping The formula stored in this node. Should be null for artifact nodes.
     * @param label A text label containing information to identify the node (such as the corresponding source code).
     */
    public DiffNode(DiffType diffType, NodeType nodeType,
                    DiffLineNumber fromLines, DiffLineNumber toLines,
                    Node featureMapping, String label) {
        this(diffType, nodeType, fromLines, toLines, featureMapping,
                new ArrayList<String>(Arrays.asList(StringUtils.LINEBREAK_REGEX.split(label, -1))));
    }

    /**
     * The same as {@link DiffNode#DiffNode(DiffType, NodeType, DiffLineNumber, DiffLineNumber, Node, String)}
     * but with the label separated into different lines of text instead of as a single String with newlines.
     */
    public DiffNode(DiffType diffType, NodeType nodeType,
                    DiffLineNumber fromLines, DiffLineNumber toLines,
                    Node featureMapping, List<String> lines) {
        children[BEFORE.ordinal()] = new ArrayList<>();
        children[AFTER.ordinal()] = new ArrayList<>();

        this.diffType = diffType;
        this.nodeType = nodeType;
        this.from = fromLines;
        this.to = toLines;
        this.featureMapping = featureMapping;
        this.lines = lines;
    }

    /**
     * Creates a new root node.
     * The root is a neutral annotation (i.e., its feature mapping is "true").
     */
    public static DiffNode createRoot() {
        return new DiffNode(
                DiffType.NON,
                NodeType.IF,
                DiffLineNumber.Invalid(),
                DiffLineNumber.Invalid(),
                FixTrueFalse.True,
                new ArrayList<>()
        );
    }

    /**
     * Creates an artifact node with the given parameters.
     * For parameter descriptions, see {@link DiffNode#DiffNode(DiffType, NodeType, DiffLineNumber, DiffLineNumber, Node, String)}.
     * The <code>code</code> parameter will be set as the node's label.
     */
    public static DiffNode createArtifact(DiffType diffType, DiffLineNumber fromLines, DiffLineNumber toLines, String code) {
        return new DiffNode(diffType, NodeType.ARTIFACT, fromLines, toLines, null, code);
    }

    /**
     * The same as {@link DiffNode#createArtifact(DiffType, DiffLineNumber, DiffLineNumber, String)} but with the code for the label
     * given as a list of individual lines instead of a single String with linebreaks to identify newlines.
     */
    public static DiffNode createArtifact(DiffType diffType, DiffLineNumber fromLines, DiffLineNumber toLines, List<String> lines) {
        return new DiffNode(diffType, NodeType.ARTIFACT, fromLines, toLines, null, lines);
    }

    /**
     * Adds the given lines to the source code lines of this node.
     * @param lines Lines to add.
     */
    public void addLines(final List<String> lines) {
        this.lines.addAll(lines);
    }

    /**
     * Returns the lines in the diff that are represented by this DiffNode.
     * The returned list is unmodifiable.
     */
    public List<String> getLabelLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Returns the lines in the diff that are represented by this DiffNode as a single text.
     * @see DiffNode#getLabelLines
     */
    public String getLabel() {
        return String.join(StringUtils.LINEBREAK, lines);
    }

    /**
     * Sets the the lines in the diff that are represented by this DiffNode to the given code.
     * Lines are identified by linebreak characters.
     */
    public void setLabel(String label) {
        lines.clear();
        Collections.addAll(lines, StringUtils.LINEBREAK_REGEX.split(label, -1));
    }

    /**
     * Gets the first {@code if} node in the path from the root to this node at the time
     * {@code time}.
     * @return The first {@code if} node in the path to the root at the time {@code time}
     */
    public DiffNode getIfNode(Time time) {
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
     * Sets the parent at {@code time} checking that this node doesn't currently have a parent.
     */
    private void setParent(final DiffNode newParent, Time time) {
        Assert.assertTrue(getParent(time) == null);
        parents[time.ordinal()] = newParent;
    }

    /**
     * Adds this subtree below the given parents.
     * Inverse of drop.
     * @param newBeforeParent Node that should be this node's before parent. May be null.
     * @param newAfterParent Node that should be this node's after parent. May be null.
     * @return True iff this node could be added as child to at least one of the given non-null parents.
     */
    public boolean addBelow(final DiffNode newBeforeParent, final DiffNode newAfterParent) {
        boolean success = false;
        if (newBeforeParent != null) {
            success |= newBeforeParent.addChild(this, BEFORE);
        }
        if (newAfterParent != null) {
            success |= newAfterParent.addChild(this, AFTER);
        }
        return success;
    }

    /**
     * Removes this subtree from its parents.
     * Inverse of addBelow.
     */
    public void drop() {
        Time.forAll(time -> {
            drop(time);
        });
    }

    /**
     * Removes this subtree from its parents at the time {@code time}.
     */
    public void drop(Time time) {
        if (getParent(time) != null) {
            getParent(time).removeChild(this, time);
        }
    }

    /**
     * Remove this node as the parent of {@code child} but it doesn't change {@link children}.
     */
    private void dropChild(final DiffNode child, Time time) {
        Assert.assertTrue(child.getParent(time) == this);
        child.parents[time.ordinal()] = null;
    }

    /**
     * Returns the index of the given child in the list of children of this node.
     * Returns -1 if the given node is not a child of this node.
     */
    public int indexOfChild(final DiffNode child, Time time) {
        return children[time.ordinal()].indexOf(child);
    }

    /**
     * Insert {@code child} as child at the time {@code time} at the position {@code index}.
     */
    public boolean insertChild(final DiffNode child, int index, Time time) {
        if (child.getDiffType().existsAtTime(time)) {
            if (child.getParent(time) != null) {
                throw new IllegalArgumentException("Given child " + child + " already has a " + time + " parent (" + child.getParent(time) + ")!");
            }

            children[time.ordinal()].add(index, child);
            child.setParent(this, time);
            return true;
        }
        return false;
    }

    /**
     * The same as {@link DiffNode#insertChild} but puts the node at the end of the children
     * list instead of inserting it at a specific index.
     */
    public boolean addChild(final DiffNode child, Time time) {
        if (child.getDiffType().existsAtTime(time)) {
            if (child.getParent(time) != null) {
                throw new IllegalArgumentException("Given child " + child + " already has a " + time + " parent (" + child.getParent(time) + ")!");
            }

            children[time.ordinal()].add(child);
            child.setParent(this, time);
            return true;
        }
        return false;
    }

    /**
     * Adds all given nodes at the time {@code time} as children using {@link DiffNode#addChild}.
     * @param children Nodes to add as children.
     * @param time whether to add {@code children} before or after the edit
     */
    public void addChildren(final Collection<DiffNode> children, Time time) {
        for (final DiffNode child : children) {
            addChild(child, time);
        }
    }

    /**
     * Removes the given node from this node's children before or after the edit.
     * The node might still remain a child after or before the edit.
     * @param child the child to remove
     * @param time whether {@code child} should be removed before or after the edit
     * @return True iff the child was removed, false iff it's not a child at {@code time}.
     */
    public boolean removeChild(final DiffNode child, Time time) {
        if (isChild(child, time)) {
            dropChild(child, time);
            children[time.ordinal()].remove(child);
            return true;
        }
        return false;
    }

    /**
     * Removes all given children for all times.
     * None of the given nodes will be a child, neither before nor after the edit, afterwards.
     * @param childrenToRemove Nodes that should not be children of this node anymore.
     */
    public void removeChildren(final Collection<DiffNode> childrenToRemove) {
        for (final DiffNode childToRemove : childrenToRemove) {
            Time.forAll(time -> removeChild(childToRemove, time));
        }
    }

    /**
     * Removes all children before or after the edit.
     * Afterwards, this node will have no children at the given time.
     * @param time whether to remove all children before or after the edit
     * @return All removed children.
     */
    public List<DiffNode> removeChildren(Time time) {
        for (var child : children[time.ordinal()]) {
            dropChild(child, time);
        }

        final List<DiffNode> orphans = children[time.ordinal()];
        children[time.ordinal()] = new ArrayList<>();
        return orphans;
    }

    /**
     * Removes all children from the given node and adds them as children to this node at the respective times.
     * The order of children is not stable because first all before children are transferred and then all after children.
     * The given node will have no children afterwards.
     * @param other The node whose children should be stolen.
     */
    public void stealChildrenOf(final DiffNode other) {
        Time.forAll(time -> addChildren(other.removeChildren(time), time));
    }

    /**
     * Returns the parent of this node before or after the edit.
     */
    public DiffNode getParent(Time time) {
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
        from = from.withLineNumberAtTime(lineRange.getFromInclusive(), time);
        to = to.withLineNumberAtTime(lineRange.getToExclusive(), time);
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
    public List<DiffNode> getChildOrder(Time time) {
        return Collections.unmodifiableList(children[time.ordinal()]);
    }

    /**
     * Returns an efficient stream representation of all direct children without duplicates.
     * In particular, children which are both before and after children of this node are only
     * contained once. The order of the children is unspecified.
     */
    public Stream<DiffNode> getAllChildrenStream() {
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
    public Iterable<DiffNode> getAllChildren() {
        return getAllChildrenStream()::iterator;
    }

    /**
     * Returns a new set with all children of this node without duplicates.
     * @see getAllChildren
     */
    public Set<DiffNode> getAllChildrenSet() {
        var result = new HashSet<DiffNode>();
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
    public boolean isChild(DiffNode child) {
        return isChild(child, BEFORE) || isChild(child, AFTER);
    }

    /**
     * Returns true iff this node is the parent of the given node at the given time.
     */
    public boolean isChild(DiffNode child, Time time) {
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
        return nodeType;
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
        id |= nodeType.ordinal();
        return id;
    }

    /**
     * Reconstructs a node from the given id and sets the given label.
     * An id uniquely determines a node's {@link DiffNode#nodeType}, {@link DiffNode#diffType}, and {@link DiffLineNumber#inDiff line number in the diff}.
     * The almost-inverse function is {@link DiffNode#getID()} but the conversion is not lossless.
     * @param id The id from which to reconstruct the node.
     * @param label The label the node should have.
     * @return The reconstructed DiffNode.
     */
    public static DiffNode fromID(int id, String label) {
        final int nodeTypeBitmask = (1 << NodeType.getRequiredBitCount()) - 1;
        final int nodeTypeOrdinal = id & nodeTypeBitmask;
        id >>= NodeType.getRequiredBitCount();

        final int diffTypeBitmask = (1 << DiffType.getRequiredBitCount()) - 1;
        final int diffTypeOrdinal = id & diffTypeBitmask;
        id >>= DiffType.getRequiredBitCount();

        final int fromInDiff = id - 1;

        var nodeType = NodeType.values()[nodeTypeOrdinal];
        return new DiffNode(
                DiffType.values()[diffTypeOrdinal],
                nodeType,
                new DiffLineNumber(fromInDiff, DiffLineNumber.InvalidLineNumber, DiffLineNumber.InvalidLineNumber),
                DiffLineNumber.Invalid(),
                nodeType.isConditionalAnnotation() ? FixTrueFalse.True : null,
                label
        );
    }

    /**
     * Checks that the DiffTree is in a valid state.
     * In particular, this method checks that all edges are well-formed (e.g., edges can be inconsistent because edges are double-linked).
     * This method also checks that a node with exactly one parent was edited, and that a node with exactly two parents was not edited.
     * @see Assert#assertTrue
     * @throws AssertionError when an inconsistency is detected.
     */
    public void assertConsistency() {
        // check consistency of children lists and edges
        for (final DiffNode c : getAllChildren()) {
            Assert.assertTrue(isChild(c), () -> "Child " + c + " of " + this + " is neither a before nor an after child!");
            Time.forAll(time -> {
                if (c.getParent(time) != null) {
                    Assert.assertTrue(c.getParent(time).isChild(c, time), () -> "The parent " + time.toString().toLowerCase() + " the edit of " + c + " doesn't contain that node as child");
                }
            });
        }

        final DiffNode pb = getParent(BEFORE);
        final DiffNode pa = getParent(AFTER);

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
            Assert.assertTrue(this.getFormula() == null, "Node with type " + nodeType + " has a non null feature mapping");
        }
    }

    /**
     * Returns a view of this {@code DiffNode} as a variation node at the time {@code time}.
     *
     * <p>See the {@code project} function in section 3.1 of
     * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
     * our paper</a>.
     */
    public Projection projection(Time time) {
        Assert.assertTrue(getDiffType().existsAtTime(time));

        if (projections[time.ordinal()] == null) {
            projections[time.ordinal()] = new Projection(this, time);
        }

        return projections[time.ordinal()];
    }

    /**
     * Transforms a {@code VariationNode} into a {@code DiffNode} by diffing {@code variationNode}
     * to itself. Acts on only the given node and does not perform recursive translations.
     */
    public static <T extends VariationNode<T>> DiffNode unchangedFlat(VariationNode<T> variationNode) {
        int from = variationNode.getLineRange().getFromInclusive();
        int to = variationNode.getLineRange().getToExclusive();

        return new DiffNode(
                DiffType.NON,
                variationNode.getNodeType(),
                new DiffLineNumber(from, from, from),
                new DiffLineNumber(to, to, to),
                variationNode.getFormula(),
                variationNode.getLabelLines()
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
    public static <T extends VariationNode<T>> DiffNode unchanged(
            final Function<? super T, DiffNode> convert,
            VariationNode<T> variationNode) {

        var diffNode = convert.apply(variationNode.upCast());

        for (var variationChildNode : variationNode.getChildren()) {
            var diffChildNode = unchanged(convert, variationChildNode);
            Time.forAll(time -> diffNode.addChild(diffChildNode, time));
        }

        return diffNode;
    }

    /**
     * Transforms a {@code VariationNode} into a {@code DiffNode} by diffing {@code variationNode}
     * to itself. Recursively translates all children.
     *
     * This is the inverse of {@link projection} iff the original {@link DiffNode} wasn't modified
     * (all node had a {@link getDiffType diff type} of {@link DiffType#NON}).
     */
    public static <T extends VariationNode<T>> DiffNode unchanged(VariationNode<T> variationNode) {
        return unchanged(DiffNode::unchangedFlat, variationNode);
    }

    @Override
    public String toString() {
        String s;
        if (isArtifact()) {
            s = String.format("%s_%s from %d to %d", diffType, nodeType, from.inDiff(), to.inDiff());
        } else if (isRoot()) {
            s = "ROOT";
        } else {
            s = String.format("%s_%s from %d to %d with \"%s\"", diffType, nodeType,
                    from.inDiff(), to.inDiff(), featureMapping);
        }
        return s;
    }
}

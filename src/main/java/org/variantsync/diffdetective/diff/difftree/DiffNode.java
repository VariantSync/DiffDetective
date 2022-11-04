package org.variantsync.diffdetective.diff.difftree;

import org.prop4j.And;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.Lines;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

/**
 * Implementation of a node in a {@link DiffTree}.
 * A DiffNode represents a single node within a variation tree diff (according to our ESEC/FSE'22 paper), but is specialized
 * to the target domain of preprocessor-based software product lines.
 * Thus, opposed to the generic mathematical model of variation tree diffs, a DiffNode always stores lines of text, line numbers, and child ordering information as its label.
 * Each DiffNode may be edited according to its {@link DiffType} and represents a source code element according to its {@link NodeType}.
 * DiffNode's store parent and child information to build a graph.
 * @author Paul Bittner, Sören Viegener, Benjamin Moosherr
 */
public class DiffNode {
    private static final short ID_OFFSET = 3;

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
     * The parent {@link DiffNode} before the edit.
     *
     * Invariant: Iff {@code beforeParent != null} then
     * {@code beforeParent.childOrder.contains(this)}.
     */
    private DiffNode beforeParent;

    /**
     * The parent {@link DiffNode} after the edit.
     *
     * Invariant: Iff {@code afterParent != null} then
     * {@code afterParent.childOrder.contains(this)}.
     */
    private DiffNode afterParent;

    /**
     * We use a list for children to maintain order.
     *
     * Invariant: Iff {@code childOrder.contains(child)} then
     * {@code child.beforeParent == this || child.afterParent == this}.
     *
     * Note that it's explicitly allowed to have
     * {@code child.beforeParent == this && child.afterParent == this}.
     */
    private final List<DiffNode> childOrder;

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
        this.childOrder = new ArrayList<>();

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
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * Returns the lines in the diff that are represented by this DiffNode as a single text.
     * @see DiffNode#getLines
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
     * Gets the first if node in the path following the before parent
     * @return The first if node in the path following the before parent
     */
    public DiffNode getBeforeIfNode() {
        if (isIf()) {
            return this;
        }
        if (isRoot()) {
            return null;
        }
        return beforeParent.getBeforeIfNode();
    }

    /**
     * Gets the first if node in the path following the after parent
     * @return The first if node in the path following the after parent
     */
    public DiffNode getAfterIfNode() {
        if (isIf()) {
            return this;
        }
        if (isRoot()) {
            return null;
        }
        return afterParent.getAfterIfNode();
    }

    /**
     * Gets the depth of the diff tree following the before parent
     * @return the depth of the diff tree following the before parent
     */
    public int getBeforeAnnotationDepth(){
        if (isRoot()) {
            return 0;
        }

        if (isIf()) {
            return beforeParent.getBeforeAnnotationDepth() + 1;
        }

        return beforeParent.getBeforeAnnotationDepth();
    }

    /**
     * Gets the depth of the diff tree following the after parent
     * @return the depth of the diff tree following the after parent
     */
    public int getAfterAnnotationDepth(){
        if (isRoot()) {
            return 0;
        }

        if (isIf()) {
            return afterParent.getAfterAnnotationDepth() + 1;
        }

        return afterParent.getAfterAnnotationDepth();
    }

    /**
     * Gets the depth of the diff tree following the before parent
     * @return the depth of the diff tree following the before parent
     */
    public int getBeforeDepth(){
        if (isRoot()) {
            return 0;
        }

        return beforeParent.getBeforeDepth() + 1;
    }

    /**
     * Gets the depth of the diff tree following the after parent
     * @return the depth of the diff tree following the after parent
     */
    public int getAfterDepth(){
        if (isRoot()) {
            return 0;
        }

        return afterParent.getAfterDepth() + 1;
    }

    /**
     * Returns true iff the path's in parent direction following the before parent and after parent
     * are the very same.
     */
    public boolean beforePathEqualsAfterPath() {
        if (beforeParent == afterParent) {
            if (beforeParent == null) {
                // root
                return true;
            }

            return beforeParent.beforePathEqualsAfterPath();
        }

        return false;
    }

    /**
     * Returns the number of unique child nodes.
     */
    public int getTotalNumberOfChildren() {
        return childOrder.size();
    }

    /**
     * Gets the amount of nodes with diff type REM in the path following the before parent
     * @return the amount of nodes with diff type REM in the path following the before parent
     */
    public int getRemAmount() {
        if (isRoot()) {
            return 0;
        }

        if (isIf() && diffType.equals(DiffType.REM)) {
            return beforeParent.getRemAmount() + 1;
        }

        if ((isElif() || isElse()) && diffType.equals(DiffType.REM)) {
            // if this is a removed elif or else we do not want to count the other branches of
            // this annotation
            // we thus go up the tree until we get the next if and continue with the parent of it
            return beforeParent.getBeforeIfNode().beforeParent.getRemAmount() + 1;
        }

        return beforeParent.getRemAmount();
    }

    /**
     * Gets the amount of nodes with diff type ADD in the path following the after parent
     * @return the amount of nodes with diff type ADD in the path following the after parent
     */
    public int getAddAmount() {
        if (isRoot()) {
            return 0;
        }

        if (isIf() && diffType.equals(DiffType.ADD)) {
            return afterParent.getAddAmount() + 1;
        }

        if ((isElif() || isElse()) && diffType.equals(DiffType.ADD)) {
            // if this is an added elif or else we do not want to count the other branches of
            // this annotation
            // we thus go up the tree until we get the next if and continue with the parent of it
            return afterParent.getAfterIfNode().afterParent.getAddAmount() + 1;
        }

        return afterParent.getAddAmount();
    }

    private void setBeforeParent(final DiffNode newBeforeParent) {
        Assert.assertTrue(beforeParent == null);
        this.beforeParent = newBeforeParent;
    }

    private void setAfterParent(final DiffNode newAfterParent) {
        Assert.assertTrue(afterParent == null);
        this.afterParent = newAfterParent;
    }

    /**
     * Adds thus subtree below the given parents.
     * Inverse of drop.
     * @param newBeforeParent Node that should be this node's before parent. May be null.
     * @param newAfterParent Node that should be this node's after parent. May be null.
     * @return True iff this node could be added as child to at least one of the given non-null parents.
     */
    public boolean addBelow(final DiffNode newBeforeParent, final DiffNode newAfterParent) {
        boolean success = false;
        if (newBeforeParent != null) {
            success |= newBeforeParent.addBeforeChild(this);
        }
        if (newAfterParent != null) {
            success |= newAfterParent.addAfterChild(this);
        }
        return success;
    }

    /**
     * Removes this subtree from its parents.
     * Inverse of addBelow.
     */
    public void drop() {
        if (beforeParent != null) {
            beforeParent.removeBeforeChild(this);
        }
        if (afterParent != null) {
            afterParent.removeAfterChild(this);
        }
    }

    private void dropBeforeChild(final DiffNode child) {
        Assert.assertTrue(child.beforeParent == this);
        child.beforeParent = null;
    }

    private void dropAfterChild(final DiffNode child) {
        Assert.assertTrue(child.afterParent == this);
        child.afterParent = null;
    }

    /**
     * Returns the index of the given child in the list of children of thus node.
     * Returns -1 if the given node is not a child of this node.
     */
    public int indexOfChild(final DiffNode child) {
        return childOrder.indexOf(child);
    }

    /**
     * Adds the given node for the given time at the given index as the child.
     * @param child The new child to add. This node should not be a child of another node for the given time.
     * @param index The index at which the node should be inserted into the children list.
     * @param time The time at which this node should be the parent of this node.
     *             For example, if the time is BEFORE, then this node will become the before parent of the given node.
     * @return True iff the insertion was successful. False iff the child could not be added.
     * @see DiffNode#insertBeforeChild
     * @see DiffNode#insertAfterChild
     */
    public boolean insertChildAt(final DiffNode child, int index, Time time) {
        return switch (time) {
            case BEFORE -> insertBeforeChild(child, index);
            case AFTER -> insertAfterChild(child, index);
        };
    }

    /**
     * The same as {@link DiffNode#insertChildAt} but the time fixed to BEFORE.
     */
    public boolean insertBeforeChild(final DiffNode child, int index) {
        if (!child.isAdd()) {
            if (!isChild(child)) {
                childOrder.add(index, child);
            }
            child.setBeforeParent(this);
            return true;
        }
        return false;
    }

    /**
     * The same as {@link DiffNode#insertChildAt} but the time fixed to AFTER.
     */
    public boolean insertAfterChild(final DiffNode child, int index) {
        if (!child.isRem()) {
            if (!isChild(child)) {
                childOrder.add(index, child);
            }
            child.setAfterParent(this);
            return true;
        }
        return false;
    }

    /**
     * The same as {@link DiffNode#insertBeforeChild} but puts the node at the end of the children
     * list instead of inserting it at a specific index.
     */
    public boolean addBeforeChild(final DiffNode child) {
        if (!child.isAdd()) {
            if (child.beforeParent != null) {
                throw new IllegalArgumentException("Given child " + child + " already has a before parent (" + child.beforeParent + ")!");
            }

            if (!isChild(child)) {
                childOrder.add(child);
            }
            child.setBeforeParent(this);
            return true;
        }
        return false;
    }

    /**
     * The same as {@link DiffNode#insertAfterChild} but puts the node at the end of the children
     * list instead of inserting it at a specific index.
     */
    public boolean addAfterChild(final DiffNode child) {
        if (!child.isRem()) {
            if (child.afterParent != null) {
                throw new IllegalArgumentException("Given child " + child + " already has an after parent (" + child.afterParent + ")!");
            }

            if (!isChild(child)) {
                childOrder.add(child);
            }
            child.setAfterParent(this);
            return true;
        }
        return false;
    }

    /**
     * Adds all given nodes as before children using {@link DiffNode#addBeforeChild}.
     * @param beforeChildren Nodes to add as children before the edit.
     */
    public void addBeforeChildren(final Collection<DiffNode> beforeChildren) {
        for (final DiffNode beforeChild : beforeChildren) {
            addBeforeChild(beforeChild);
        }
    }

    /**
     * Adds all given nodes as after children using {@link DiffNode#addAfterChild}.
     * @param afterChildren Nodes to add as children after the edit.
     */
    public void addAfterChildren(final Collection<DiffNode> afterChildren) {
        for (final DiffNode afterChild : afterChildren) {
            addAfterChild(afterChild);
        }
    }

    /**
     * Removes the given node from this node's children before the edit.
     * The node might still remain a child after the edit.
     * @param child The child to remove before the edit.
     * @return True iff the child was removed, false iff it was no before child.
     */
    public boolean removeBeforeChild(final DiffNode child) {
        if (isBeforeChild(child)) {
            dropBeforeChild(child);
            removeFromCache(child);
            return true;
        }
        return false;
    }
    
    /**
     * Removes the given node from this node's children after the edit.
     * The node might still remain a child before the edit.
     * @param child The child to remove after the edit.
     * @return True iff the child was removed, false iff it was no after child.
     */
    public boolean removeAfterChild(final DiffNode child) {
        if (isAfterChild(child)) {
            dropAfterChild(child);
            removeFromCache(child);
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
            removeBeforeChild(childToRemove);
            removeAfterChild(childToRemove);
        }
    }

    /**
     * Removes all children before the edit.
     * Afterwards, this node will have no before children.
     * @return All removed before children.
     */
    public List<DiffNode> removeBeforeChildren() {
        final List<DiffNode> orphans = new ArrayList<>();

        // Note that the following method call can't be written using a foreach loop reusing
        // {@code removeBeforeChild} because lists can't be modified during traversal.
        childOrder.removeIf(child -> {
            if (!isBeforeChild(child)) {
                return false;
            }

            orphans.add(child);
            dropBeforeChild(child);
            return !isAfterChild(child);
        });

        return orphans;
    }


    /**
     * Removes all children after the edit.
     * Afterwards, this node will have no after children.
     * @return All removed after children.
     */
    public List<DiffNode> removeAfterChildren() {
        final List<DiffNode> orphans = new ArrayList<>();

        // Note that the following method call can't be written using a foreach loop reusing
        // {@code removeAfterChild} because lists can't be modified during traversal.
        childOrder.removeIf(child -> {
            if (!isAfterChild(child)) {
                return false;
            }

            orphans.add(child);
            dropAfterChild(child);
            return !isBeforeChild(child);
        });

        return orphans;
    }

    /**
     * If the given node is neither a before nor after child, it will be removed
     * from the internal cache that stores the order of children.
     * This method does nothing the given node is (still) a child.
     * @param child The node to remove from the order cache if it is no child.
     * @see DiffNode#isChild(DiffNode)
     */
    private void removeFromCache(final DiffNode child) {
        if (!isChild(child)) {
            childOrder.remove(child);
        }
    }

    /**
     * Removes all children from the given node and adds them as children to this node at the respective times.
     * The order of children is not stable because first all before children are transferred and then all after children.
     * The given node will have no children afterwards.
     * @param other The node whose children should be stolen.
     */
    public void stealChildrenOf(final DiffNode other) {
        addBeforeChildren(other.removeBeforeChildren());
        addAfterChildren(other.removeAfterChildren());
    }

    /**
     * Returns the parent of this node before the edit.
     */
    public DiffNode getBeforeParent() {
        return beforeParent;
    }

    /**
     * Returns the parent of this node after the edit.
     */
    public DiffNode getAfterParent() {
        return afterParent;
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
    public Lines getLinesInDiff() {
        return DiffLineNumber.rangeInDiff(from, to);
    }

    /**
     * Returns the range of line numbers of this node's corresponding source code before the edit.
     * @see DiffLineNumber#rangeBeforeEdit
     */
    public Lines getLinesBeforeEdit() {
        return DiffLineNumber.rangeBeforeEdit(from, to);
    }

    /**
     * Returns the range of line numbers of this node's corresponding source code after the edit.
     * @see DiffLineNumber#rangeAfterEdit
     */
    public Lines getLinesAfterEdit() {
        return DiffLineNumber.rangeAfterEdit(from, to);
    }

    /**
     * Returns the formula that is stored in this node.
     * The formula is null for artifact nodes (i.e., {@link NodeType#ARTIFACT}).
     * The formula is not null for mapping nodes
     * @see NodeType#isAnnotation
     */
    public Node getDirectFeatureMapping() {
        return featureMapping;
    }

    /**
     * Returns the list representing the order of the children.
     * Any child occurs exactly once, even if this node is it's before and after parent.
     */
    public List<DiffNode> getChildOrder() {
        return Collections.unmodifiableList(childOrder);
    }

    /**
     * Legacy alias for {@link DiffNode#getChildOrder()}.
     */
    public List<DiffNode> getAllChildren() {
        return getChildOrder();
    }

    /**
     * Returns the full feature mapping formula of this node.
     * The feature mapping of an {@link NodeType#IF} node is its {@link DiffNode#getDirectFeatureMapping direct feature mapping}.
     * The feature mapping of {@link NodeType#ELSE} and {@link NodeType#ELIF} nodes is determined by all formulas in the respective if-elif-else chain.
     * The feature mapping of an {@link NodeType#ARTIFACT artifact} node is the feature mapping of its parent.
     * See Equation (1) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @param parentOf Function that returns the parent of a node.
     *                 This function decides whether the before or after parent should be visited.
     *                 It thus decides whether to compute the feature mapping before or after the edit.
     * @return The feature mapping of this node for the given parent edges.
     *         The returned list represents a conjunction (i.e., all clauses should be combined with boolean AND).
     */
    private List<Node> getFeatureMappingClauses(final Function<DiffNode, DiffNode> parentOf) {
        final DiffNode parent = parentOf.apply(this);

        if (isElse() || isElif()) {
            List<Node> and = new ArrayList<>();

            if (isElif()) {
                and.add(getDirectFeatureMapping());
            }

            // Negate all previous cases
            DiffNode ancestor = parent;
            while (!ancestor.isIf()) {
                if (ancestor.isElif()) {
                    and.add(negate(ancestor.getDirectFeatureMapping()));
                } else {
                    throw new RuntimeException("Expected If or Elif above Else or Elif but got " + ancestor.nodeType + " from " + ancestor);
                    // Assert.assertTrue(ancestor.isArtifact());
                }
                ancestor = parentOf.apply(ancestor);
            }
            and.add(negate(ancestor.getDirectFeatureMapping()));

            return and;
        } else if (isArtifact()) {
            return parent.getFeatureMappingClauses(parentOf);
        }

        return List.of(getDirectFeatureMapping());
    }

    /**
     * Same as {@link DiffNode#getFeatureMappingClauses} but conjuncts the returned clauses to a single formula.
     */
    private Node getFeatureMapping(Function<DiffNode, DiffNode> parentOf) {
        final List<Node> fmClauses = getFeatureMappingClauses(parentOf);
        if (fmClauses.size() == 1) {
            return fmClauses.get(0);
        }
        return new And(fmClauses);
    }

    /**
     * Returns the full feature mapping formula of this node before the edit.
     * The feature mapping of an {@link NodeType#IF} node is its {@link DiffNode#getDirectFeatureMapping direct feature mapping}.
     * The feature mapping of {@link NodeType#ELSE} and {@link NodeType#ELIF} nodes is determined by all formulas in the respective if-elif-else chain.
     * The feature mapping of an {@link NodeType#ARTIFACT artifact} node is the feature mapping of its parent.
     * See Equation (1) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @return The feature mapping of this node for the given parent edges.
     */
    public Node getBeforeFeatureMapping() {
        return getFeatureMapping(DiffNode::getBeforeParent);
    }

    /**
     * Returns the full feature mapping formula of this node after the edit.
     * The feature mapping of an {@link NodeType#IF} node is its {@link DiffNode#getDirectFeatureMapping direct feature mapping}.
     * The feature mapping of {@link NodeType#ELSE} and {@link NodeType#ELIF} nodes is determined by all formulas in the respective if-elif-else chain.
     * The feature mapping of an {@link NodeType#ARTIFACT artifact} node is the feature mapping of its parent.
     * See Equation (1) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @return The feature mapping of this node for the given parent edges.
     */
    public Node getAfterFeatureMapping() {
        return getFeatureMapping(DiffNode::getAfterParent);
    }

    /**
     * Depending on the given time, returns either the
     * {@link DiffNode#getBeforeFeatureMapping() before feature mapping} or
     * {@link DiffNode#getAfterFeatureMapping() after feature mapping}.
     */
    public Node getFeatureMapping(Time time) {
        return time.match(
                this::getBeforeFeatureMapping,
                this::getAfterFeatureMapping
        );
    }

    /**
     * Returns the presence condition of this node for the respective time.
     * See Equation (2) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @param parentOf Function that returns the parent of a node.
     *                 This function decides whether the before or after parent should be visited.
     *                 It thus decides whether to compute the feature mapping before or after the edit.
     * @return The presence condition of this node for the given parent edges.
     *         The returned list represents a conjunction (i.e., all clauses should be combined with boolean AND).
     */
    private List<Node> getPresenceCondition(Function<DiffNode, DiffNode> parentOf) {
        final DiffNode parent = parentOf.apply(this);

        if (isElse() || isElif()) {
            final List<Node> clauses = new ArrayList<>(getFeatureMappingClauses(parentOf));

            // Find corresponding if
            DiffNode correspondingIf = parent;
            while (!correspondingIf.isIf()) {
                correspondingIf = parentOf.apply(correspondingIf);
            }

            // If this elif-else-chain was again nested in another annotation, add its pc.
            final DiffNode outerNesting = parentOf.apply(correspondingIf);
            if (outerNesting != null) {
                clauses.addAll(outerNesting.getPresenceCondition(parentOf));
            }

            return clauses;
        } else if (isArtifact()) {
            return parent.getPresenceCondition(parentOf);
        }

        // this is mapping or root
        final List<Node> clauses;
        if (parent == null) {
            clauses = new ArrayList<>(1);
        } else {
            clauses = parent.getPresenceCondition(parentOf);
        }
        clauses.add(featureMapping);
        return clauses;
    }

    /**
     * Returns the presence condition of this node before the edit.
     * See Equation (2) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @return The presence condition of this node for the given parent edges.
     */
    public Node getBeforePresenceCondition() {
        if (diffType.existsBefore()) {
            return new And(getPresenceCondition(DiffNode::getBeforeParent));
        } else {
            throw new WrongTimeException("Cannot determine before PC of added node " + this);
        }
    }

    /**
     * Returns the presence condition of this node after the edit.
     * See Equation (2) in our paper (+ its extension to time for variation tree diffs described in Section 3.1).
     * @return The presence condition of this node for the given parent edges.
     */
    public Node getAfterPresenceCondition() {
        if (diffType.existsAfter()) {
            return new And(getPresenceCondition(DiffNode::getAfterParent));
        } else {
            throw new WrongTimeException("Cannot determine after PC of removed node " + this);
        }
    }

    /**
     * Depending on the given time, returns either the
     * {@link DiffNode#getBeforePresenceCondition() before presence condition} or
     * {@link DiffNode#getAfterPresenceCondition() after presence condition}.
     */
    public Node getPresenceCondition(Time time) {
        return time.match(
                this::getBeforePresenceCondition,
                this::getAfterPresenceCondition
        );
    }

    /**
     * Returns true iff this node is the before parent of the given node.
     */
    public boolean isBeforeChild(DiffNode child) {
        return child.beforeParent == this;
    }

    /**
     * Returns true iff this node is the after parent of the given node.
     */
    public boolean isAfterChild(DiffNode child) {
        return child.afterParent == this;
    }

    /**
     * Returns true iff this node is the before or after parent of the given node.
     */
    public boolean isChild(DiffNode child) {
        return isBeforeChild(child) || isAfterChild(child);
    }

    /**
     * Returns true iff this node is the parent of the given node at the given time.
     */
    public boolean isChild(DiffNode child, Time time) {
        return time.match(isBeforeChild(child), isAfterChild(child));
    }

    /**
     * Returns true iff this node has no children.
     */
    public boolean isLeaf() {
        return childOrder.isEmpty();
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

    /**
     * Returns true if this node represents an ELIF annotation.
     * @see NodeType#ELIF
     */
    public boolean isElif() {
        return this.nodeType.equals(NodeType.ELIF);
    }

    /**
     * Returns true if this node represents a conditional annotation.
     * @see NodeType#IF
     */
    public boolean isIf() {
        return this.nodeType.equals(NodeType.IF);
    }

    /**
     * Returns true if this node is an artifact node.
     * @see NodeType#ARTIFACT
     */
    public boolean isArtifact() {
        return this.nodeType.equals(NodeType.ARTIFACT);
    }

    /**
     * Returns true if this node represents an ELSE annotation.
     * @see NodeType#ELSE
     */
    public boolean isElse() {
        return this.nodeType.equals(NodeType.ELSE);
    }

    /**
     * Returns true if this node is a root node (has no parents).
     */
    public boolean isRoot() {
        return getBeforeParent() == null && getAfterParent() == null;
    }

    /**
     * Returns {@link NodeType#isAnnotation()} for this node's {@link DiffNode#nodeType}.
     */
    public boolean isAnnotation() {
        return this.nodeType.isAnnotation();
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
        int lineNumber = 1 + from.inDiff();
        Assert.assertTrue((lineNumber << 2*ID_OFFSET) >> 2*ID_OFFSET == lineNumber);

        int id;
        id = lineNumber;
        id <<= ID_OFFSET;
        id += diffType.ordinal();
        id <<= ID_OFFSET;
        id += nodeType.ordinal();
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
    public static DiffNode fromID(final int id, String label) {
        final int lowestBitsMask = (1 << ID_OFFSET) - 1;

        final int nodeTypeOrdinal = id & lowestBitsMask;
        final int diffTypeOrdinal = (id >> ID_OFFSET) & lowestBitsMask;
        final int fromInDiff      = (id >> (2*ID_OFFSET)) - 1;

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
        for (final DiffNode c : childOrder) {
            Assert.assertTrue(isChild(c), () -> "Child " + c + " of " + this + " is neither a before nor an after child!");
            if (c.getBeforeParent() != null) {
                Assert.assertTrue(c.getBeforeParent().isBeforeChild(c), () -> "The beforeParent of " + c + " doesn't contain that node as child");
            }
            if (c.getAfterParent() != null) {
                Assert.assertTrue(c.getAfterParent().isAfterChild(c), () -> "The afterParent of " + c + " doesn't contain that node as child");
            }
        }

        // a node with exactly one parent was edited
        if (beforeParent == null && afterParent != null) {
            Assert.assertTrue(isAdd());
        }
        if (beforeParent != null && afterParent == null) {
            Assert.assertTrue(isRem());
        }
        // a node with exactly two parents was not edited
        if (beforeParent != null && afterParent != null) {
            Assert.assertTrue(isNon());
        }

        // Else and Elif nodes have an If or Elif as parent.
        if (this.isElse() || this.isElif()) {
            if (beforeParent != null) {
                Assert.assertTrue(beforeParent.isIf() || beforeParent.isElif(), "Before parent " + beforeParent + " of " + this + " is neither IF nor ELIF!");
            }
            if (afterParent != null) {
                Assert.assertTrue(afterParent.isIf() || afterParent.isElif(), "After parent " + afterParent + " of " + this + " is neither IF nor ELIF!");
            }
        }

        // Only if and elif nodes have a formula
        if (this.isIf() || this.isElif()) {
            Assert.assertTrue(this.getDirectFeatureMapping() != null, "If or elif without feature mapping!");
        } else {
            Assert.assertTrue(this.getDirectFeatureMapping() == null, "Node with type " + nodeType + " has a non null feature mapping");
        }
    }

    /**
     * Prepends the {@link DiffType#symbol} of the given diffType to all given lines and
     * joins all lines with {@link StringUtils#LINEBREAK linebreaks} to a single text.
     * @param diffType The change type of the given diff hunk.
     * @param lines The lines to turn into a text-based diff.
     * @return A diff in which all given lines have the given diff type.
     */
    public static String toTextDiffLine(final DiffType diffType, final List<String> lines) {
        return lines.stream().collect(Collectors.joining(StringUtils.LINEBREAK + diffType.symbol, diffType.symbol, ""));
    }

    /**
     * Unparses this node's lines into its original text-based diff.
     * @return The diff from which this node was parsed, reconstructed as accurately as possible.
     */
    public String toTextDiffLine() {
        return toTextDiffLine(diffType, lines);
    }

    /**
     * Unparses this subgraph into its original text-based diff.
     * This will return the diff of the entire subgraph starting with this node as root.
     * Recursively invokes {@link DiffNode#toTextDiffLine()} on this node and all its descendants.
     * @return The diff from which this subgraph was parsed, reconstructed as accurately as possible.
     */
    public String toTextDiff() {
        final StringBuilder diff = new StringBuilder();

        if (!this.isRoot()) {
            diff
                    .append(this.toTextDiffLine())
                    .append(StringUtils.LINEBREAK);
        }

        for (final DiffNode child : childOrder) {
            diff.append(child.toTextDiff());
        }

        // Add endif after macro
        if (isAnnotation() && !isRoot()) {
            diff
                    .append(toTextDiffLine(this.diffType, List.of("#endif")))
                    .append(StringUtils.LINEBREAK);
        }

        return diff.toString();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffNode diffNode = (DiffNode) o;
        return diffType == diffNode.diffType && nodeType == diffNode.nodeType && from.equals(diffNode.from) && to.equals(diffNode.to) && Objects.equals(featureMapping, diffNode.featureMapping) && lines.equals(diffNode.lines);
    }

    /**
     * Compute a hash using all available attributes.
     *
     * This implementation doesn't strictly adhere to the contract required by {@code Object},
     * because some attributes (for example the line numbers) can be changed during the lifetime of
     * a {@code DiffNode}. So when using something like a {@code HashSet} the user of {@code
     * DiffNode} has to be careful not to change any attributes of a stored {@code DiffNode}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(diffType, nodeType, from, to, featureMapping, lines);
    }
}

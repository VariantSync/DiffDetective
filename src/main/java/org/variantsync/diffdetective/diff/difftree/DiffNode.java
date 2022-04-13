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

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

/**
 * Implementation of a node of the diff tree.
 *
 * Includes methods for creating a node by getting its code type and diff type and for getting the feature mapping of the node.
 */
public class DiffNode {
	private static final short ID_OFFSET = 3;

    public DiffType diffType;
    public CodeType codeType;
    private boolean isMultilineMacro = false;

    private final DiffLineNumber from = DiffLineNumber.Invalid();
    private final DiffLineNumber to = DiffLineNumber.Invalid();

    private Node featureMapping;
    private String label;

    /**
     * The parent {@link DiffNode} before the edit.
     */
    private DiffNode beforeParent;
    
    /**
     * The parent {@link DiffNode} after the edit.
     */
    private DiffNode afterParent;

    /**
     * We use a list for children to maintain order.
     */
    private final List<DiffNode> childOrder;

    // These lists store the outgoing edges. These lists do not respect order.
    private List<DiffNode> beforeChildren;
    private List<DiffNode> afterChildren;

    private DiffNode() {
        this.childOrder = new ArrayList<>();
        this.beforeChildren = new ArrayList<>();
        this.afterChildren = new ArrayList<>();
    }

    public DiffNode(DiffType diffType, CodeType codeType,
                    DiffLineNumber fromLines, DiffLineNumber toLines,
                    Node featureMapping, String label) {
        this();
        this.diffType = diffType;
        this.codeType = codeType;
        this.from.set(fromLines);
        this.to.set(toLines);
        this.featureMapping = featureMapping;
        this.label = label;
    }

    /**
     * Creates a (new) root node
     * @return A (new) root node
     */
    public static DiffNode createRoot() {
        return new DiffNode(
                DiffType.NON,
                CodeType.ROOT,
                new DiffLineNumber(1, 1, 1),
                DiffLineNumber.Invalid(),
                FixTrueFalse.True,
                ""
        );
    }

    public static DiffNode createCode(DiffType diffType, DiffLineNumber fromLines, DiffLineNumber toLines, String code) {
        return new DiffNode(diffType, CodeType.CODE, fromLines, toLines, null, code);
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
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
     * @return The number of unique child nodes.
     */
    public int getTotalNumberOfChildren() {
        return childOrder.size();
    }

    /**
     * @return The number of edges going to children.
     *         This is the sum of the number of edges to before children and the number of edges to after children.
     */
    public int getCardinality() {
        return beforeChildren.size() + afterChildren.size();
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
        this.beforeParent = newBeforeParent;
    }

    private void setAfterParent(final DiffNode newAfterParent) {
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
        assert child.beforeParent == this;
        child.beforeParent = null;
    }

    private void dropAfterChild(final DiffNode child) {
        assert child.afterParent == this;
        child.afterParent = null;
    }

    public int indexOfChild(final DiffNode child) {
        return childOrder.indexOf(child);
    }

    public boolean insertChildAt(final DiffNode child, int index, Time time) {
        return switch (time) {
            case BEFORE -> insertBeforeChild(child, index);
            case AFTER -> insertAfterChild(child, index);
        };
    }

    public boolean insertBeforeChild(final DiffNode child, int index) {
        if (!child.isAdd()) {
            addWithoutDuplicates(beforeChildren, child);
            insertWithoutDuplicates(childOrder, child, index);
            child.setBeforeParent(this);
            return true;
        }
        return false;
    }

    public boolean insertAfterChild(final DiffNode child, int index) {
        if (!child.isRem()) {
            addWithoutDuplicates(afterChildren, child);
            insertWithoutDuplicates(childOrder, child, index);
            child.setAfterParent(this);
            return true;
        }
        return false;
    }

    public boolean addBeforeChild(final DiffNode child) {
        if (!child.isAdd()) {
            addWithoutDuplicates(beforeChildren, child);
            addWithoutDuplicates(childOrder, child);
            child.setBeforeParent(this);
            return true;
        }
        return false;
    }

    public boolean addAfterChild(final DiffNode child) {
        if (!child.isRem()) {
            addWithoutDuplicates(afterChildren, child);
            addWithoutDuplicates(childOrder, child);
            child.setAfterParent(this);
            return true;
        }
        return false;
    }

    public void addBeforeChildren(final Collection<DiffNode> beforeChildren) {
        for (final DiffNode beforeChild : beforeChildren) {
            addBeforeChild(beforeChild);
        }
    }

    public void addAfterChildren(final Collection<DiffNode> afterChildren) {
        for (final DiffNode afterChild : afterChildren) {
            addAfterChild(afterChild);
        }
    }

    private static void addWithoutDuplicates(final List<DiffNode> childrenlist, final DiffNode child) {
        if (!childrenlist.contains(child)) {
            childrenlist.add(child);
        }
    }

    private static void insertWithoutDuplicates(final List<DiffNode> childrenlist, final DiffNode child, int index) {
        if (!childrenlist.contains(child)) {
            childrenlist.add(index, child);
        }
    }

    public boolean removeBeforeChild(final DiffNode child) {
        if (this.beforeChildren.remove(child)) {
            removeFromCache(this.afterChildren, this.childOrder, child);
            dropBeforeChild(child);
            return true;
        }
        return false;
    }

    public boolean removeAfterChild(final DiffNode child) {
        if (this.afterChildren.remove(child)) {
            removeFromCache(this.beforeChildren, this.childOrder, child);
            dropAfterChild(child);
            return true;
        }
        return false;
    }

    public void removeChildren(final Collection<DiffNode> childrenToRemove) {
        for (final DiffNode childToRemove : childrenToRemove) {
            removeBeforeChild(childToRemove);
            removeAfterChild(childToRemove);
        }
    }

    public List<DiffNode> removeBeforeChildren() {
        for (final DiffNode child : beforeChildren) {
            removeFromCache(this.afterChildren, this.childOrder, child);
            dropBeforeChild(child);
        }

        final List<DiffNode> orphans = beforeChildren;
        beforeChildren = new ArrayList<>();
        return orphans;
    }

    public List<DiffNode> removeAfterChildren() {
        for (final DiffNode child : afterChildren) {
            removeFromCache(this.beforeChildren, this.childOrder, child);
            dropAfterChild(child);
        }

        final List<DiffNode> orphans = afterChildren;
        afterChildren = new ArrayList<>();
        return orphans;
    }

    private static void removeFromCache(
            final List<DiffNode> childrenTypeB,
            final List<DiffNode> allChildren,
            final DiffNode child)
    {
        if (!childrenTypeB.contains(child)) {
            allChildren.remove(child);
        }
    }

    public void stealChildrenOf(final DiffNode other) {
        addBeforeChildren(other.removeBeforeChildren());
        addAfterChildren(other.removeAfterChildren());
    }

    /**
     * @return {@link #beforeParent}
     */
    public DiffNode getBeforeParent() {
        return beforeParent;
    }

    /**
     * @return {@link #afterParent}
     */
    public DiffNode getAfterParent() {
        return afterParent;
    }

    public DiffLineNumber getFromLine() {
        return from;
    }

    public DiffLineNumber getToLine() {
        return to;
    }

    public Lines getLinesInDiff() {
        return DiffLineNumber.rangeInDiff(from, to);
    }

    public Lines getLinesBeforeEdit() {
        return DiffLineNumber.rangeBeforeEdit(from, to);
    }

    public Lines getLinesAfterEdit() {
        return DiffLineNumber.rangeAfterEdit(from, to);
    }

    public Node getDirectFeatureMapping() {
        return featureMapping;
    }

    public List<DiffNode> getChildOrder() {
        return Collections.unmodifiableList(childOrder);
    }

    public List<DiffNode> getAllChildren() {
        return getChildOrder();
    }

    public List<DiffNode> getBeforeChildren() {
        return Collections.unmodifiableList(beforeChildren);
    }

    public List<DiffNode> getAfterChildren() {
        return Collections.unmodifiableList(afterChildren);
    }

    public List<DiffNode> getChildren(Time time) {
        return time.match(getBeforeChildren(), getAfterChildren());
    }

    public void setIsMultilineMacro(boolean isMultilineMacro) {
        this.isMultilineMacro = isMultilineMacro;
    }

    public boolean isMultilineMacro() {
        return isMultilineMacro;
    }

    private List<Node> getFeatureMappingClauses(Function<DiffNode, DiffNode> parentOf) {
        final DiffNode parent = parentOf.apply(this);

        if (isElse() || isElif()) {
            List<Node> and = new ArrayList<>();

            if (isElif()) {
                and.add(featureMapping);
            }

            // Negate all previous cases
            DiffNode ancestor = parent;
            while (!ancestor.isIf()) {
                if (ancestor.isElif()) {
                    and.add(negate(ancestor.getDirectFeatureMapping()));
                } else {
                    Assert.assertTrue(ancestor.isCode());
                }
                ancestor = parentOf.apply(ancestor);
            }
            and.add(negate(ancestor.getDirectFeatureMapping()));

            return and;
        } else if (isCode()) {
            return List.of(parent.getFeatureMapping(parentOf));
        }

        return List.of(featureMapping);
    }

    private Node getFeatureMapping(Function<DiffNode, DiffNode> parentOf) {
        final List<Node> fmClauses = getFeatureMappingClauses(parentOf);
        if (fmClauses.size() == 1) {
            return fmClauses.get(0);
        }
        return new And(fmClauses);
    }

    /**
     * Gets the feature mapping of the node before the patch
     * @return the feature mapping of the node before the patch
     */
    public Node getBeforeFeatureMapping() {
        return getFeatureMapping(DiffNode::getBeforeParent);
    }

    /**
     * Gets the feature mapping of the node after the patch
     * @return the feature mapping of the node after the patch
     */
    public Node getAfterFeatureMapping() {
        return getFeatureMapping(DiffNode::getAfterParent);
    }

    public Node getFeatureMapping(Time time) {
        return time.match(
                this::getBeforeFeatureMapping,
                this::getAfterFeatureMapping
        );
    }

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
        } else if (isCode()) {
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

    public Node getBeforePresenceCondition() {
        if (diffType.existsBefore()) {
            return new And(getPresenceCondition(DiffNode::getBeforeParent));
        } else {
            throw new WrongTimeException("Cannot determine before PC of added node " + this);
        }
    }

    public Node getAfterPresenceCondition() {
        if (diffType.existsAfter()) {
            return new And(getPresenceCondition(DiffNode::getAfterParent));
        } else {
            throw new WrongTimeException("Cannot determine after PC of removed node " + this);
        }
    }

    public Node getPresenceCondition(Time time) {
        return time.match(
                this::getBeforePresenceCondition,
                this::getAfterPresenceCondition
        );
    }

    public boolean isLeaf() {
        return childOrder.isEmpty();
    }

    public boolean isRem() {
        return this.diffType.equals(DiffType.REM);
    }

    public boolean isNon() {
        return this.diffType.equals(DiffType.NON);
    }

    public boolean isAdd() {
        return this.diffType.equals(DiffType.ADD);
    }

    public boolean isElif() {
        return this.codeType.equals(CodeType.ELIF);
    }

    public boolean isIf() {
        return this.codeType.equals(CodeType.IF);
    }

    public boolean isCode() {
        return this.codeType.equals(CodeType.CODE);
    }

    public boolean isEndif() {
        return this.codeType.equals(CodeType.ENDIF);
    }

    public boolean isElse() {
        return this.codeType.equals(CodeType.ELSE);
    }

    public boolean isRoot() {
        return this.codeType.equals(CodeType.ROOT);
    }

    public boolean isMacro() {
        return this.codeType.isMacro();
    }

    /**
     * @return An integer that uniquely identifiers this DiffNode within its patch.
     */
    public int getID() {
        int id;
        id = 1 + from.inDiff;
        id <<= ID_OFFSET;
        id += diffType.ordinal();
        id <<= ID_OFFSET;
        id += codeType.ordinal();
        return id;
    }
    
    public static DiffNode fromID(final int id) {
        // lowest 8 bits
        final int lowestBitsMask = (1 << ID_OFFSET) - 1;

        final int codeTypeOrdinal = id & lowestBitsMask;
        final int diffTypeOrdinal = (id >> ID_OFFSET) & lowestBitsMask;
        final int fromInDiff      = (id >> (2*ID_OFFSET)) - 1;

        return new DiffNode(
                DiffType.values()[diffTypeOrdinal],
                CodeType.values()[codeTypeOrdinal],
                new DiffLineNumber(fromInDiff, DiffLineNumber.InvalidLineNumber, DiffLineNumber.InvalidLineNumber),
                DiffLineNumber.Invalid(),
                null,
                ""
        );
    }

    public void assertConsistency() {
        // check consistency of children lists and edges
        for (final DiffNode bc : beforeChildren) {
            Assert.assertTrue(bc.beforeParent == this, () -> "Before child " + bc + " of " + this + " has another parent " + bc.beforeParent + "!");
            Assert.assertTrue(childOrder.contains(bc), () -> "Before child " + bc + " of " + this + " is not in the list of all children!");
        }
        for (final DiffNode ac : afterChildren) {
            Assert.assertTrue(ac.afterParent == this, () -> "After child " + ac + " of " + this + " has another parent " + ac.afterParent + "!");
            Assert.assertTrue(childOrder.contains(ac), () -> "After child " + ac + " of " + this + " is not in the list of all children!");
        }
        for (final DiffNode c : childOrder) {
            Assert.assertTrue(beforeChildren.contains(c) || afterChildren.contains(c), () -> "Child " + c + " of " + this + " is neither a before not an after child!");
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
    }

    public void assertSemanticConsistency() {
        // Else and Elif nodes have an If or Elif as parent.
        if (this.isElse() || this.isElif()) {
            if (beforeParent != null) {
                Assert.assertTrue(beforeParent.isIf() || beforeParent.isElif(), "Before parent " + beforeParent + " of " + this + " is neither IF nor ELIF!");
            }
            if (afterParent != null) {
                Assert.assertTrue(afterParent.isIf() || afterParent.isElif(), "After parent " + afterParent + " of " + this + " is neither IF nor ELIF!");
            }
        }
    }

    public static String toTextDiffLine(final DiffType diffType, final String text) {
        return diffType.symbol + text.replaceAll(StringUtils.LINEBREAK_REGEX, StringUtils.LINEBREAK + diffType.symbol);
    }

    public String toTextDiffLine() {
        return toTextDiffLine(diffType, this.getLabel());
    }

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

        if (isMacro()) {
            diff
                    .append(toTextDiffLine(this.diffType, CodeType.ENDIF.asMacroText()))
                    .append(StringUtils.LINEBREAK);
        }

        return diff.toString();
    }

    @Override
    public String toString() {
        String s;
        if (isCode()) {
            s = String.format("%s_%s from %d to %d", diffType, codeType, from.inDiff, to.inDiff);
        } else if (isRoot()) {
            s = "ROOT";
        } else {
            s = String.format("%s_%s from %d to %d with \"%s\"", diffType, codeType,
                    from.inDiff, to.inDiff, featureMapping);
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffNode diffNode = (DiffNode) o;
        return isMultilineMacro == diffNode.isMultilineMacro && diffType == diffNode.diffType && codeType == diffNode.codeType && from.equals(diffNode.from) && to.equals(diffNode.to) && Objects.equals(featureMapping, diffNode.featureMapping) && label.equals(diffNode.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffType, codeType, isMultilineMacro, from, to, featureMapping, label);
    }
}

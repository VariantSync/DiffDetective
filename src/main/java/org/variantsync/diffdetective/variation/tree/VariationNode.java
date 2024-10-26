package org.variantsync.diffdetective.variation.tree;

import org.prop4j.And;
import org.prop4j.Node;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.LineRange;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffNode; // For Javadoc
import org.variantsync.diffdetective.variation.diff.Projection; // For Javadoc

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

/**
 * A base class for views of a node in a variation tree.
 *
 * <p>Provides common methods for querying variation trees and changing their structure. This class
 * doesn't provide mutation methods for attributes which may be shared between different underlying
 * nodes (for example a {@link Projection projection} of a {@link DiffNode}). Most prominently,
 * there are no methods to change the {@link getNodeType type} or the {@link getLabel label}
 * of this node.
 *
 * <p>There are many methods which are not abstract. These are convenience methods or algorithms
 * acting on variation nodes where a separate class may be undesirable (for example because they are
 * quite common or because the calling syntax {@code node.algorithm()} makes more sense than the
 * alternative {@code Algorithm.run(node)}).
 *
 * @param <T> the derived type (the type extending this class)
 * @param <L> The type of label stored in this tree.
 *
 * @see assertConsistency
 * @author Benjamin Moosherr
 */
public abstract class VariationNode<T extends VariationNode<T, L>, L extends Label> implements HasNodeType {
    /**
     * Returns this instance as the derived class type {@code T}.
     * The deriving class will only have to return {@code this} here but this can't be implemented
     * in the base class. If some derived class can't implement this method by returning
     * {@code this}, it probably violates the requirements for the type parameter {@code T} (namely
     * that it' the derived class itself).
     */
    public abstract T upCast();

    /**
     * Returns this instance as a {@link VariationNode}.
     * This is only useful for accessing private members inside of {@link VariationNode}. These
     * can't be accessed if the type of the variable of this instance is {@code T} so a down cast is
     * required. This function only exists to document this necessity and make it more readable.
     */
    public VariationNode<T, L> downCast() {
        return this;
    }

    /**
     * Returns the node type of this node which determines the type of the represented element in
     * the variation tree (e.g., mapping or artifact).
     *
     * @see HasNodeType
     */
    public abstract NodeType getNodeType();

    /**
     * Returns the label of this node.
     *
     * <p>If {@link #isArtifact()} is {@code true} this may represent the source code of this artifact.
     * Otherwise it may represent the preprocessor expression which was parsed to obtain
     * {@link #getFormula()}. In either case, this label may be an arbitrary value,
     * selected according to the needs of the user of this class.
     */
    public abstract L getLabel();

    /**
     * Returns the range of line numbers of this node's corresponding source code.
     *
     * @see #setLineRange(LineRange)
     */
    public abstract LineRange getLineRange();

    /**
     * Sets the range of line numbers of this node's corresponding source code.
     *
     * @see getLineRange
     */
    public abstract void setLineRange(LineRange lineRange);

    /**
     * Returns the parent of this node, or {@code null} if this node doesn't have a parent.
     *
     * @see drop
     * @see addBelow
     */
    public abstract T getParent();

    /**
     * Returns an unmodifiable list representing the children of this node.
     *
     * <p>The following invariant has to hold for all {@code node}s:
     * <code>
     *   for (var child : node.getChildren()) {
     *     Assert.assertTrue(node == child.getParent(node))
     *   }
     * </code>
     *
     * @see isChild
     * @see addChild
     * @see removeChild
     */
    public abstract List<T> getChildren();

    /**
     * Returns the line with the endif of the corresponding if, if the node is an if node, otherwise null
     * @return String, the Line with endif
     */
    public abstract String getEndIf();

    /**
     * Returns {@code true} iff this node has no parent.
     *
     * @see getParent
     */
    public boolean isRoot() {
        return getParent() == null;
    }

    /**
     * Returns {@code true} iff this node has no children.
     *
     * @see getChildren
     */
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    /**
     * Returns the number of child nodes.
     *
     * <p>Note: This is O(n) for {@link Projection}.
     *
     * @see getChildCount
     */
    public int getChildCount() {
        return getChildren().size();
    }

    /**
     * Computes the length of the path from the root node to this node.
     */
    public int getDepth() {
        if (isRoot()) {
            return 0;
        }

        return getParent().getDepth() + 1;
    }

    /**
     * Returns the first {@code if} node in the path from this node upwards to the root.
     */
    public T getIfNode() {
        if (isIf()) {
            return this.upCast();
        }
        return getParent().getIfNode();
    }

    /**
     * Returns {@code true} iff this node is the parent of the given node.
     *
     * @see getChildren
     */
    public boolean isChild(T child) {
        return child.getParent() == this.upCast();
    }

    /**
     * Returns the index of the given child in the list of children of this node.
     * Returns -1 if the given node is not a child of this node.
     *
     * <p>Warning: If this is a {@link Projection}, then the returned index may be different to the
     * index returned by {@link DiffNode#indexOfChild}.
     *
     * @see getChildren
     */
    public int indexOfChild(final T child) {
        return getChildren().indexOf(child);
    }

    /**
     * The same as {@link insertChild} but puts the node at the end of the children list instead of
     * inserting it at a specific index.
     *
     * @throws IllegalArgumentException if {@code child} already has a parent
     * @see getChildren
     */
    public abstract void addChild(final T child);

    /**
     * Adds a child before the given index to the children list of this node and sets its parent to
     * this node.
     *
     * <p>When calling {@link indexOfChild} with {@code child} the returned index will be
     * {@code index} as long as the children list isn't modified.
     *
     * @throws IllegalArgumentException if {@code child} already has a parent
     * @see addChildren
     */
    public abstract void insertChild(final T child, int index);

    /**
     * Adds the given nodes in traversal order as children using
     * {@link addChild}.
     * *
     * @throws IllegalArgumentException if any child of {@code children} already has a parent
     */
    public void addChildren(final Collection<T> children) {
        for (final var child : children) {
            addChild(child);
        }
    }

    /**
     * Removes the given node from this node's children list and sets the parent of {@code child}
     * to {@code null}.
     *
     * @throws IllegalArgumentException if {@code childe} is not a child of this node
     * @see removeChildren
     * @see getChildren
     */
    public abstract void removeChild(final T child);

    /**
     * Removes the given nodes from the children list using {@link removeChild}.

     * @throws IllegalArgumentException if any child in {@code childrenToRemove} is not a child of
     * this node
     * @see removeAllChildren
     */
    public void removeChildren(final Collection<T> childrenToRemove) {
        for (final var childToRemove : childrenToRemove) {
            removeChild(childToRemove);
        }
    }

    /**
     * Removes all children of this node and sets their parent to {@code null}.
     * Afterwards, this node will have no children.
     */
    public abstract void removeAllChildren();

    /**
     * Adds this subtree below the given parent.
     * Inverse of {@link drop}.
     *
     * @see addChild
     * @throws IllegalArgumentException if this node already has a parent
     */
    public void addBelow(final T parent) {
        if (parent != null) {
            parent.addChild(this.upCast());
        }
    }

    /**
     * Removes this subtree from its parents by removing it as child from its parent and settings
     * the parent of this node to {@code null}.
     * Inverse of {@link addBelow}.
     *
     * @see removeChild
     */
    public void drop() {
        if (getParent() != null) {
            getParent().removeChild(this.upCast());
        }
    }

    /**
     * Removes all children from the given node and adds them as children to this node.
     * The order of the children is preserved. The given node will have no children afterwards.
     *
     * @param other The node whose children should be stolen.
     * @see addChildren
     * @see removeAllChildren
     */
    public void stealChildrenOf(final T other) {
        addChildren(other.getChildren());
        other.removeAllChildren();
    }

    /**
     * Returns the formula that is stored in this node.
     * The formula is not {@code null} for
     * {@link NodeType#isConditionalAnnotation mapping nodes with annotations} and {@code null}
     * otherwise ({@link NodeType#ARTIFACT}, {@link NodeType#ELSE}).
     *
     * <p>If the type parameter {@code T} of this class is not a concrete variation tree, then the
     * returned {@link Node formula} should be treated as unmodifiable to prevent undesired side
     * effects (e.g., to {@link DiffNode}s).
     */
    public abstract Node getFormula();

    /**
     * Same as {@link getFeatureMapping} but returns a list of formulas representing a conjunction.
     */
    private List<Node> getFeatureMappingClauses() {
        final var parent = getParent();

        if (isElse() || isElif()) {
            List<Node> and = new ArrayList<>();

            if (isElif()) {
                and.add(getFormula());
            }

            // Negate all previous cases
            var ancestor = parent;
            while (!ancestor.isIf()) {
                if (ancestor.isElif()) {
                    and.add(negate(ancestor.getFormula()));
                } else {
                    throw new RuntimeException("Expected If or Elif above Else or Elif but got " + ancestor.getNodeType() + " from " + ancestor);
                    // Assert.assertTrue(ancestor.isArtifact());
                }
                ancestor = ancestor.getParent();
            }
            and.add(negate(ancestor.getFormula()));

            return and;
        } else if (isArtifact()) {
            return parent.downCast().getFeatureMappingClauses();
        }

        return List.of(getFormula());
    }

    /**
     * Returns the full feature mapping formula of this node.
     *
     * <p>The feature mapping of an {@link NodeType#IF} node is its {@link #getFormula()
     * direct feature mapping}. The feature mapping of {@link NodeType#ELSE} and {@link
     * NodeType#ELIF} nodes is determined by all formulas in the respective if-elif-else chain. The
     * feature mapping of an {@link NodeType#ARTIFACT artifact} node is the feature mapping of its
     * parent. See Equation (1) in
     * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
     * our paper</a>.
     *
     * @return the feature mapping of this node
     */
    public Node getFeatureMapping() {
        final List<Node> fmClauses = getFeatureMappingClauses();
        if (fmClauses.size() == 1) {
            return fmClauses.get(0);
        }
        return new And(fmClauses);
    }

    /**
     * Returns the presence condition clauses of this node.
     *
     * @return a list representing a conjunction (i.e., all clauses should be combined with boolean
     * AND)
     * @see getPresenceCondition
     */
    private List<Node> getPresenceConditionClauses() {
        final var parent = getParent();

        if (isElse() || isElif()) {
            final List<Node> clauses = new ArrayList<>(getFeatureMappingClauses());

            // Find corresponding if
            var correspondingIf = parent;
            while (!correspondingIf.isIf()) {
                correspondingIf = correspondingIf.getParent();
            }

            // If this elif-else-chain was again nested in another annotation, add its pc.
            final var outerNesting = correspondingIf.getParent();
            if (outerNesting != null) {
                clauses.addAll(outerNesting.downCast().getPresenceConditionClauses());
            }

            return clauses;
        } else if (isArtifact()) {
            return parent.downCast().getPresenceConditionClauses();
        }

        // this is mapping or root
        final List<Node> clauses;
        if (parent == null) {
            clauses = new ArrayList<>(1);
        } else {
            clauses = parent.downCast().getPresenceConditionClauses();
        }
        clauses.add(getFormula());
        return clauses;
    }

    /**
     * Returns the presence condition of this node.
     * See Equation (2) in
     * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
     * our paper</a>.
     */
    public Node getPresenceCondition() {
        final List<Node> pcClauses = getPresenceConditionClauses();
        if (pcClauses.size() == 1) {
            return pcClauses.get(0);
        }
        return new And(pcClauses);
    }

    /**
     * Traverses all nodes in this subtree in preorder.
     */
    public void forAllPreorder(Consumer<T> action) {
        action.accept(this.upCast());

        for (var child : getChildren()) {
            child.forAllPreorder(action);
        }
    }

    public void forMeAndMyAncestors(final Consumer<T> action) {
        action.accept(this.upCast());
        final T p = getParent();
        if (p != null) {
            p.forMeAndMyAncestors(action);
        }
    }

    /**
     * Checks whether any node in this subtree satisfies the given condition.
     * The condition might not be invoked on every node in case a node is found.
     * @param condition A condition to check on each node.
     * @return True iff the given condition returns true for at least one node in this tree.
     */
    public boolean anyMatch(final Predicate<? super T> condition) {
        if (condition.test(this.upCast())) {
            return true;
        }

        for (var child : getChildren()) {
            if (child.anyMatch(condition)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a copy of this variation tree in a {@link VariationTreeNode concrete variation tree implementation}.
     * If the type parameter {@code T} of this class is {@link VariationTreeNode} then this is
     * effectively a deep copy.
     */
    public VariationTreeNode<L> toVariationTree() {
        return toVariationTree(new HashMap<>());
    }

    /**
     * Returns a copy of this variation tree in a {@link VariationTreeNode concrete variation tree implementation}.
     * If the type parameter {@code T} of this class is {@link VariationTreeNode} then this is
     * effectively a deep copy.
     *
     * <p>The map {@code oldToNew} should be empty as it will be filled by this method. After the
     * method call, the map keys will contain all nodes in this node's subtree (including this
     * node). The corresponding values will be the nodes in the returned node's subtree (including
     * the returned node), where each pair (k, v) denotes that v was cloned from k.
     *
     * @param oldToNew A map that memorizes the translation of individual nodes.
     * @return A deep copy of this tree.
     */
    public VariationTreeNode<L> toVariationTree(final Map<? super T, VariationTreeNode<L>> oldToNew) {
        // Copy mutable attributes to allow modifications of the new node.
        var newNode = new VariationTreeNode<L>(
            getNodeType(),
            getFormula() == null ? null : getFormula().clone(),
            getLineRange(),
            getLabel()
        );
        oldToNew.put(this.upCast(), newNode);

        for (var child : getChildren()) {
            newNode.addChild(child.toVariationTree(oldToNew));
        }

        return newNode;
    }

    /**
    * Checks that this node satisfies some easy to check invariants.
    * In particular, this method checks that
    * <ul>
    * <li>if-chains are nested correctly,
    * <li>the root is an {@link NodeType#IF} with the feature mapping {@code "true"},
    * <li>the feature mapping is {@code null} iff {@code isConditionalAnnotation} is {@code false}
    * and
    * <li>all edges are well-formed (e.g., edges can be inconsistent because edges are
    * double-linked).
    * </ul>
    *
    * <p>Some invariants are not checked. These include
    * <ul>
    * <li>There should be no cycles and
    * <li>{@link getID} should be unique in the whole variation tree.
    * </ul>
    *
    * @see Assert#assertTrue
    * @throws AssertionError when an inconsistency is detected.
    */
    public void assertConsistency() {
        // ELSE and ELIF nodes have an IF or ELIF as parent.
        if (isElse() || isElif()) {
            Assert.assertTrue(
                getParent().isIf() || getParent().isElif(),
                "Parent " + getParent() + " of " + this + " is neither IF nor ELIF");
        }

        // Presence/absence of the direct feature mapping
        if (isConditionalAnnotation()) {
            Assert.assertTrue(
                getFormula() != null,
                "The conditional annotation " + this + " doesn't have a direct feature mapping");
        } else {
            Assert.assertTrue(
                getFormula() == null,
                "The node " + this + " shouldn't have a direct feature mapping");
        }

        // The root has to be an IF
        if (isRoot()) {
            Assert.assertTrue(
                isIf(),
                "The root has to be an IF");

            Assert.assertTrue(
                getFormula().equals(FixTrueFalse.True),
                "The root has to have the feature mapping 'true'");
        }

        // check consistency of children lists and edges
        for (var child : getChildren()) {
            Assert.assertTrue(
                child.getParent() == this.upCast(), () ->
                "The parent (" + this + ") of " + child + " is not set correctly");
        }
    }



    /**
     * Returns an integer that uniquely identifies this node within its tree.
     *
     * <p>Some attributes may be recovered from this ID but this depends on the derived class. For
     * example {@link VariationTreeNode#fromID} can recover {@link getNodeType} and
     * {@link getLineRange the start line number}. Beware that {@link Projection} returns
     * {@link DiffNode#getID} so this id is not fully compatible with
     * {@link VariationTreeNode#getID}.
     */
    public abstract int getID();

    /**
     * Unparses the labels of this subtree into {@code output}.
     *
     * <p>This method assumes that all labels of this subtree represent source code lines.
     */
    public void printSourceCode(final StringBuilder output) {
        for (final String line : getLabel().getLines()) {
            output.append(line);
            output.append(StringUtils.LINEBREAK);
        }

        for (final var child : getChildren()) {
            child.printSourceCode(output);
        }

        // Add #endif after macro
        if (isIf() && !isRoot()) {
            output.append("#endif");
            output.append(StringUtils.LINEBREAK);
        }
    }

    /**
     * Returns true if this subtree is exactly equal to {@code other}.
     * This check uses equality checks instead of identity.
     */
    public boolean isSameAs(VariationNode<T, L> other) {
        if (!shallowIsSameAs(other)) {
            return false;
        }

        var childIt = getChildren().iterator();
        var otherChildIt = other.getChildren().iterator();
        while (childIt.hasNext() && otherChildIt.hasNext()) {
            if (!childIt.next().isSameAs(otherChildIt.next())) {
                return false;
            }
        }

        return childIt.hasNext() == otherChildIt.hasNext();
    }

    /**
     * Returns true if this node is exactly equal to {@code other} without checking any children.
     * This check uses equality checks instead of identity.
     */
    protected boolean shallowIsSameAs(VariationNode<T, L> other) {
        return
            this.getNodeType().equals(other.getNodeType()) &&
            this.getLabel().equals(other.getLabel()) &&
            this.getLineRange().equals(other.getLineRange());
    }
}

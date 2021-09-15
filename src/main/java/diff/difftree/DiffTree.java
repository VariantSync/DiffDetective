package diff.difftree;

import diff.difftree.parse.DiffTreeParser;
import diff.difftree.traverse.DiffTreeTraversal;
import diff.difftree.traverse.DiffTreeVisitor;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of the diff tree.
 * Contains lists of all code nodes and all annotation nodes
 */
public class DiffTree {
    private final DiffNode root;
    private final List<DiffNode> codeNodes;
    private final List<DiffNode> annotationNodes;

    public DiffTree(DiffNode root, List<DiffNode> codeNodes, List<DiffNode> annotationNodes) {
        this.root = root;
        this.codeNodes = codeNodes;
        this.annotationNodes = annotationNodes;
    }

    public static DiffTree fromFile(final Path p, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) throws IOException {
        final String fullDiff = IO.readAsString(p);
        return DiffTreeParser.createDiffTree(fullDiff, collapseMultipleCodeLines, ignoreEmptyLines);
    }

    public DiffTree forAll(final Consumer<DiffNode> procedure) {
        DiffTreeTraversal.forAll(procedure).visit(this);
        return this;
    }

    public DiffTree traverse(final DiffTreeVisitor visitor) {
        DiffTreeTraversal.with(visitor).visit(this);
        return this;
    }

    public DiffNode getRoot() {
        return root;
    }

    /**
     * Adds the given subtree to this DiffTree below the given parents.
     * Assumes that beforeParent and afterParent are contained in this DiffTree but subtreeRoot is not.
     * @param subtreeRoot The root of the subtree to be added below the given parents.
     * @param beforeParent A node in this DiffTree that should become subtreeRoots before parent. May be null.
     * @param afterParent A node in this DiffTree that should become subtreeRoots after parent. May be null.
     * @return True iff subtreeRoot could be added as child to at least one of the given non-null parents.
     */
    public boolean addSubtree(final DiffNode subtreeRoot, final DiffNode beforeParent, final DiffNode afterParent) {
        // assert beforeParent and afterParent are in the tree
        // assert subtreeRoot is not in the tree
        boolean result = subtreeRoot.addBelow(beforeParent, afterParent);
        addToNodesRecursively(subtreeRoot);
        return result;
    }

    /**
     * Adds the given subtree to this DiffTree below the given parents.
     * Assumes that beforeParent and afterParent are contained in this DiffTree but subtreeRoot is not.
     * Does not add the descendants of subtreeRoot to the lists of nodes in this DiffTree.
     * @param subtreeRoot The root of thesubtree to be added below the given parents.
     * @param beforeParent A node in this DiffTree that should become subtreeRoots before parent. May be null.
     * @param afterParent A node in this DiffTree that should become subtreeRoots after parent. May be null.
     * @return True iff subtreeRoot could be added as child to at least one of the given non-null parents.
     */
    public boolean addSubtreeRoot(final DiffNode subtreeRoot, final DiffNode beforeParent, final DiffNode afterParent) {
        // assert beforeParent and afterParent are in the tree
        // assert subtreeRoot is not in the tree
        addToNodes(subtreeRoot);
        return subtreeRoot.addBelow(beforeParent, afterParent);
    }

    /**
     * Disconnects the subtree rooted at the given node from this tree.
     * @param subtreeRoot The root of the subtree to remove.
     */
    public void removeSubtree(final DiffNode subtreeRoot) {
        subtreeRoot.drop();
        removeFromNodesRecursively(subtreeRoot);
    }

    /**
     * Disconnects the subtree rooted at the given node from this tree.
     * Removes all children from subtreeRoot.
     * @param subtreeRoot The root of the subtree to remove.
     * @return The abandoned children of the subtree. These children are still being counted as being in this DiffTree
     *         but are disconnected from the main tree and thus form own connected components.
     */
    public Collection<DiffNode> removeSubtreeRoot(final DiffNode subtreeRoot) {
        // assert subtreeRoot is in the tree
        subtreeRoot.drop();
        removeFromNodes(subtreeRoot);
        return subtreeRoot.removeChildren();
    }

    private void addToNodes(final DiffNode node) {
        if (node.isCode()) {
            codeNodes.add(node);
        } else if (node.isMacro()) {
            annotationNodes.add(node);
        } else {
            assert node.isRoot();
            throw new IllegalArgumentException("Cannot add DiffTree root but tried to with " + node + "!");
        }
    }

    private void addToNodesRecursively(final DiffNode node) {
        addToNodes(node);
        node.getChildren().forEach(this::addToNodesRecursively);
    }

    private void removeFromNodes(final DiffNode node) {
        if (node.isCode()) {
            codeNodes.remove(node);
        } else if (node.isMacro()) {
            annotationNodes.remove(node);
        } else {
            assert node.isRoot();
            throw new IllegalArgumentException("Cannot remove DiffTree root but tried to with " + node + "!");
        }
    }

    private void removeFromNodesRecursively(final DiffNode node) {
        removeFromNodes(node);
        node.getChildren().forEach(this::removeFromNodesRecursively);
    }

    public List<DiffNode> getCodeNodes() {
        return codeNodes;
    }

    public List<DiffNode> getAnnotationNodes() {
        return annotationNodes;
    }

    public int size() {
        return 1 /*Root*/ + codeNodes.size() + annotationNodes.size();
    }

    public boolean isEmpty() {
        return size() == 1;
    }
}

package diff.data.transformation;

import diff.data.DiffNode;
import diff.data.DiffTree;

import java.util.ArrayList;
import java.util.List;

public class CollapseNonEditedSubtrees implements DiffTreeTransformer {
    private List<DiffNode> removedNodes;

    @Override
    public void transform(DiffTree diffTree) {
        removedNodes = new ArrayList<>();
        collapse(diffTree.getRoot());
        diffTree.getAnnotationNodes().removeAll(removedNodes);
        diffTree.getCodeNodes().removeAll(removedNodes);
        removedNodes = null;
    }

    private boolean collapse(DiffNode subtree) {
        if (subtree.isNon()) {
            // If all children are collapsable / can be collapsed and are only children of this node ...
            for (final DiffNode child : subtree.getChildren()) {
                if (!(
                        collapse(child)
                        && child.getAfterParent() == subtree
                        && child.getBeforeParent() == subtree))
                {
                    // then this is not a collapsable child.
                    return false;
                }
            }

            // ... remove all children.
            removedNodes.addAll(subtree.getChildren());
            subtree.dropChildren();
            return true;
        }

        return false;
    }
}

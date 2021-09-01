package diff.data;

import java.util.List;

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

    public DiffNode getRoot() {
        return root;
    }

    public List<DiffNode> getCodeNodes() {
        return codeNodes;
    }

    public List<DiffNode> getAnnotationNodes() {
        return annotationNodes;
    }
}

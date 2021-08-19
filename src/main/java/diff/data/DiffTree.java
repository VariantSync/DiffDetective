package diff.data;

import java.util.List;

/**
 * Implementation of the diff tree.
 * Contains lists of all code nodes and all annotation nodes
 */
public class DiffTree {
    private final List<DiffNode> codeNodes;
    private final List<DiffNode> annotationNodes;

    private final DiffNode root;

    public DiffTree(DiffNode root, List<DiffNode> codeNodes, List<DiffNode> annotationNodes) {
        this.root = root;
        this.codeNodes = codeNodes;
        this.annotationNodes = annotationNodes;
    }

    public List<DiffNode> getCodeNodes() {
        return codeNodes;
    }

    public List<DiffNode> getAnnotationNodes() {
        return annotationNodes;
    }
}

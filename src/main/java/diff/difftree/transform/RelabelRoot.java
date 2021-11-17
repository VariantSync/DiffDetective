package diff.difftree.transform;

import diff.difftree.DiffTree;

public class RelabelRoot implements DiffTreeTransformer {
    private final String newLabel;

    public RelabelRoot(final String newLabel) {
        this.newLabel = newLabel;
    }

    @Override
    public void transform(DiffTree diffTree) {
        diffTree.getRoot().setLabel(newLabel);
    }
}

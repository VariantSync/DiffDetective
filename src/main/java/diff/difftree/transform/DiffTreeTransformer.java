package diff.difftree.transform;

import diff.difftree.DiffTree;

@FunctionalInterface
public interface DiffTreeTransformer {
    void transform(DiffTree diffTree);
}
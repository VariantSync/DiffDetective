package diff.data.transformation;

import diff.data.DiffTree;

@FunctionalInterface
public interface DiffTreeTransformer {
    void transform(DiffTree diffTree);
}

package diff.difftree.transform;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record FeatureExpressionFilter(Predicate<DiffNode> isFeatureAnnotation) implements DiffTreeTransformer {
    @Override
    public void transform(DiffTree diffTree) {
        final List<DiffNode> illegalNodes = new ArrayList<>();
        diffTree.forAll(node -> {
            if (node.isMacro() && !isFeatureAnnotation.test(node)) {
                illegalNodes.add(node);
            }
        });

        for (final DiffNode illegalMacro : illegalNodes) {
            diffTree.removeNode(illegalMacro);
        }
    }
}

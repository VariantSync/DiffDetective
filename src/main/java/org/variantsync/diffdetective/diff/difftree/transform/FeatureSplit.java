package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.Set;

public class FeatureSplit {
    private DiffTree inputDiff;
    private Set<DiffTree> subtrees;

    public FeatureSplit(DiffTree diffTree) {
        inputDiff = diffTree;
       //subtrees = Set.of(
       //        inputDiff.forAll(node -> {
       //            if (node.isAdd() || node.isRem()) {
       //                return generateSubtree(node, inputDiff);
       //            }
       //        })
       //);
    }

    public DiffTree generateSubtree(DiffNode node, DiffTree initDiffTree) {
        DiffTree copy = new Duplication().deepClone(initDiffTree);
        node.getAllChildren().forEach(
                child -> child.isRem()
        );
        return null;
    }
}

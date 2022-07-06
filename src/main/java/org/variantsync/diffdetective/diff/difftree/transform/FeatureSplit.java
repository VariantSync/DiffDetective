package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        node.getAllChildren().forEach(
                child -> child.isRem()
        );
        return null;
    }
}

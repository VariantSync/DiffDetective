package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.diff.difftree.Time;
import org.variantsync.diffdetective.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Starfold implements DiffTreeTransformer {
    private final boolean respectNodeOrder;

    private Starfold(boolean respectNodeOrder) {
        this.respectNodeOrder = respectNodeOrder;
    }

    public static Starfold RespectNodeOrder() {
        return new Starfold(true);
    }

    public static Starfold IgnoreNodeOrder() {
        return new Starfold(false);
    }

    @Override
    public void transform(DiffTree diffTree) {
        // All non-artifact nodes are potential roots of stars.
        final List<DiffNode> macroNodes = diffTree.computeAllNodesThat(node -> !node.isCode());
//        System.out.println("Inspecting " + macroNodes.size() + " star root candidates.");
        for (DiffNode macro : macroNodes) {
            if (isStarRoot(macro)) {
//                System.out.println("Found star root " + macro);
                foldStar(macro);
            }
        }
    }

    public void foldStar(final DiffNode starRoot) {
        // We fold the stars for each time respectively.
        Time.forall(t -> foldStarAtTime(starRoot, t));
    }

    public void foldStarAtTime(final DiffNode starRoot, Time time) {
//        System.out.println("Fold " + starRoot + " at time " + time);
        final DiffType targetDiffType = DiffType.thatExistsOnlyAt(time);

        final List<DiffNode> children = starRoot.getChildren(time);
        final List<DiffNode> starArms = new ArrayList<>();

        for (DiffNode child : children) {
            if (isStarArm(child) && child.diffType == targetDiffType) {
//                System.out.println("  Found arm " + child);
                starArms.add(child);
            } else if (respectNodeOrder && !starArms.isEmpty() && child.isNon()) {
                // If we find a non-edited node, we cannot continue grouping arm nodes without invalidating the order
                // of the nodes. We thus have to merge and restart after the non-edited node.
//                System.out.println("  Found blocker " + child);
                mergeArms(starRoot, time, targetDiffType, starArms);
                starArms.clear();
            }
        }

        mergeArms(starRoot, time, targetDiffType, starArms);
    }

    public void mergeArms(final DiffNode starRoot, Time time, final DiffType targetDiffType, final List<DiffNode> starArms) {
        // If there is more than one arm, merge.
        if (starArms.size() > 1) {
            final int targetIndex = starRoot.indexOfChild(starArms.get(0));
            starRoot.removeChildren(starArms);
            starRoot.insertChildAt(
                    DiffNode.createCode(
                            targetDiffType,
                            DiffLineNumber.Invalid(),
                            DiffLineNumber.Invalid(),
                            starArms.stream().map(DiffNode::getLabel).collect(Collectors.joining(StringUtils.LINEBREAK))
                    ),
                    targetIndex,
                    time
            );
        }
    }

    public static boolean isStarRoot(final DiffNode node) {
        return !node.isCode() && node.isNon();
    }

    public static boolean isStarArm(final DiffNode node) {
        return node.isLeaf() && node.isCode();
    }
}

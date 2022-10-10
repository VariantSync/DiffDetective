package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.functjonal.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds artifact nodes whose label is exactly equal. If one of those nodes was added and the other one was removed,
 * NaiveMovedArtifactDetection merges them and interprets this edit as a move instead of separate insertion and deletion.
 * A possible future extension would be to account for multiline artifact nodes to not only check for exact equality of text
 * but for each line in the nodes individually.
 * @author Paul Bittner
 */
public class NaiveMovedArtifactDetection implements DiffTreeTransformer {
    @Override
    public void transform(final DiffTree diffTree) {
        final List<Pair<DiffNode, DiffNode>> twins = findArtifactTwins(diffTree);

        for (final Pair<DiffNode, DiffNode> twin : twins) {
            final DiffNode added;
            final DiffNode removed;

            // Determine which one is the added and which is the removed node.
            if (twin.first().isAdd()) {
                added = twin.first();
                removed = twin.second();
            } else {
                added = twin.second();
                removed = twin.first();
            }

            final DiffNode afterParent = added.getAfterParent();
            final DiffNode beforeParent = removed.getBeforeParent();
            final DiffNode mergedNode = merge(added, removed);

            added.drop();
            removed.drop();
            mergedNode.stealChildrenOf(added);
            mergedNode.stealChildrenOf(removed);
            mergedNode.addBelow(beforeParent, afterParent);
        }
    }

    private static List<Pair<DiffNode, DiffNode>> findArtifactTwins(final DiffTree diffTree) {
        final List<DiffNode> artifactNodes = diffTree.computeArtifactNodes();
        final List<Pair<DiffNode, DiffNode>> twins = new ArrayList<>();

        while (!artifactNodes.isEmpty()) {
            // Always inspect last element as it's the cheapest to remove.
            final DiffNode artifact = artifactNodes.get(artifactNodes.size() - 1);
            artifactNodes.remove(artifact);

            // If the node was inserted or removed ...
            if (!artifact.isNon()) {
                // ... check if the opposite operation was applied to the same artifact somewhere else.
                final DiffNode twin = findTwinOf(artifact, artifactNodes);
                if (twin != null) {
                    twins.add(new Pair<>(artifact, twin));
                    artifactNodes.remove(twin);
                }
            }
        }

        return twins;
    }

    private static DiffNode findTwinOf(final DiffNode artifact, final List<DiffNode> artifactNodes) {
        final DiffType weAreLookingFor = artifact.diffType.inverse();
        final String text = artifact.getLabel().trim();

        if (text.isEmpty()) {
            return null;
        }

        // We assert the following as we removed the artifact node in transform.
        // assert(!artifactNodes.contains(artifact));
        for (final DiffNode other : artifactNodes) {
            if (other.diffType == weAreLookingFor && text.equals(other.getLabel().trim())) {
                return other;
            }
        }

        return null;
    }

    private static DiffNode merge(final DiffNode added, final DiffNode removed) {
        final DiffLineNumber addFrom = added.getFromLine();
        final DiffLineNumber remFrom = removed.getFromLine();
        final DiffLineNumber addTo = added.getToLine();
        final DiffLineNumber remTo = removed.getToLine();

        final DiffLineNumber from = new DiffLineNumber(Math.min(addFrom.inDiff(), remFrom.inDiff()), remFrom.beforeEdit(), addFrom.afterEdit());
        final DiffLineNumber to = new DiffLineNumber(Math.max(addTo.inDiff(), remTo.inDiff()), remTo.beforeEdit(), addTo.afterEdit());

        return DiffNode.createArtifact(DiffType.NON, from, to, added.getLabel() /* equals removed.getText() */);
    }
}

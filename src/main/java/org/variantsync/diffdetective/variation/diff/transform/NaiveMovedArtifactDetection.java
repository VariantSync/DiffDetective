package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.functjonal.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Finds artifact nodes whose label is exactly equal. If one of those nodes was added and the other one was removed,
 * NaiveMovedArtifactDetection merges them and interprets this edit as a move instead of separate insertion and deletion.
 * A possible future extension would be to account for multiline artifact nodes to not only check for exact equality of text
 * but for each line in the nodes individually.
 * @author Paul Bittner
 */
public class NaiveMovedArtifactDetection<L extends Label> implements DiffTreeTransformer<L> {
    @Override
    public void transform(final DiffTree<L> diffTree) {
        final List<Pair<DiffNode<L>, DiffNode<L>>> twins = findArtifactTwins(diffTree);

        for (final Pair<DiffNode<L>, DiffNode<L>> twin : twins) {
            final DiffNode<L> added;
            final DiffNode<L> removed;

            // Determine which one is the added and which is the removed node.
            if (twin.first().isAdd()) {
                added = twin.first();
                removed = twin.second();
            } else {
                added = twin.second();
                removed = twin.first();
            }

            final DiffNode<L> afterParent = added.getParent(AFTER);
            final DiffNode<L> beforeParent = removed.getParent(BEFORE);
            final DiffNode<L> mergedNode = merge(added, removed);

            added.drop();
            removed.drop();
            mergedNode.stealChildrenOf(added);
            mergedNode.stealChildrenOf(removed);
            mergedNode.addBelow(beforeParent, afterParent);
        }
    }

    private static <L extends Label> List<Pair<DiffNode<L>, DiffNode<L>>> findArtifactTwins(final DiffTree<L> diffTree) {
        final List<DiffNode<L>> artifactNodes = diffTree.computeArtifactNodes();
        final List<Pair<DiffNode<L>, DiffNode<L>>> twins = new ArrayList<>();

        while (!artifactNodes.isEmpty()) {
            // Always inspect last element as it's the cheapest to remove.
            final DiffNode<L> artifact = artifactNodes.get(artifactNodes.size() - 1);
            artifactNodes.remove(artifact);

            // If the node was inserted or removed ...
            if (!artifact.isNon()) {
                // ... check if the opposite operation was applied to the same artifact somewhere else.
                final DiffNode<L> twin = findTwinOf(artifact, artifactNodes);
                if (twin != null) {
                    twins.add(new Pair<>(artifact, twin));
                    artifactNodes.remove(twin);
                }
            }
        }

        return twins;
    }

    private static <L extends Label> DiffNode<L> findTwinOf(final DiffNode<L> artifact, final List<DiffNode<L>> artifactNodes) {
        final DiffType weAreLookingFor = artifact.diffType.inverse();
        final String text = artifact.getLabel().toString().trim();

        if (text.isEmpty()) {
            return null;
        }

        // We assert the following as we removed the artifact node in transform.
        // assert(!artifactNodes.contains(artifact));
        for (final DiffNode<L> other : artifactNodes) {
            if (other.diffType == weAreLookingFor && text.equals(other.getLabel().toString().trim())) {
                return other;
            }
        }

        return null;
    }

    private static <L extends Label> DiffNode<L> merge(final DiffNode<L> added, final DiffNode<L> removed) {
        final DiffLineNumber addFrom = added.getFromLine();
        final DiffLineNumber remFrom = removed.getFromLine();
        final DiffLineNumber addTo = added.getToLine();
        final DiffLineNumber remTo = removed.getToLine();

        final DiffLineNumber from = new DiffLineNumber(Math.min(addFrom.inDiff(), remFrom.inDiff()), remFrom.beforeEdit(), addFrom.afterEdit());
        final DiffLineNumber to = new DiffLineNumber(Math.max(addTo.inDiff(), remTo.inDiff()), remTo.beforeEdit(), addTo.afterEdit());

        return DiffNode.createArtifact(DiffType.NON, from, to, added.getLabel() /* equals removed.getText() */);
    }
}

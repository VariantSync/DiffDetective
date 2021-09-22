package diff.difftree.transform;

import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.DiffLineNumber;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.DiffType;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds code nodes whose text is exactly equal. If one of those nodes was added and the other one was removed,
 * MovedCodeDetection merges them and interprets this edit as a move instead of separate insertion and deletion.
 * A possible future extension would be to account for multiline code nodes to not only check for exact equality of text
 * but for each line in the nodes individually.
 */
public class NaiveMovedCodeDetection implements DiffTreeTransformer {
    @Override
    public void transform(final DiffTree diffTree) {
        final List<Pair<DiffNode, DiffNode>> twins = findCodeTwins(diffTree);

        for (final Pair<DiffNode, DiffNode> twin : twins) {
            final DiffNode added;
            final DiffNode removed;

            // Determine which one is the added and which is the removed node.
            if (twin.getKey().isAdd()) {
                added = twin.getKey();
                removed = twin.getValue();
            } else {
                added = twin.getValue();
                removed = twin.getKey();
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

    private static List<Pair<DiffNode, DiffNode>> findCodeTwins(final DiffTree diffTree) {
        final List<DiffNode> codeNodes = diffTree.computeCodeNodes();
        final List<Pair<DiffNode, DiffNode>> twins = new ArrayList<>();

        while (!codeNodes.isEmpty()) {
            // Always inspect last element as it's the cheapest to remove.
            final DiffNode code = codeNodes.get(codeNodes.size() - 1);
            codeNodes.remove(code);

            // If the node was inserted or removed ...
            if (!code.isNon()) {
                // ... check if the opposite operation was applied to the same code somewhere else.
                final DiffNode twin = findTwinOf(code, codeNodes);
                if (twin != null) {
                    twins.add(new Pair<>(code, twin));
                    codeNodes.remove(twin);
                }
            }
        }

        return twins;
    }

    private static DiffNode findTwinOf(final DiffNode code, final List<DiffNode> codeNodes) {
        final DiffType weAreLookingFor = code.diffType.inverse();
        final String text = code.getText().trim();

        if (text.isEmpty()) {
            return null;
        }

        // We assert the following as we removed the code node in transform.
        // assert(!codeNodes.contains(code));
        for (final DiffNode other : codeNodes) {
            if (other.diffType == weAreLookingFor && text.equals(other.getText().trim())) {
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

        final DiffLineNumber from = new DiffLineNumber(Math.min(addFrom.inDiff, remFrom.inDiff), remFrom.beforeEdit, addFrom.afterEdit);
        final DiffLineNumber to = new DiffLineNumber(Math.max(addTo.inDiff, remTo.inDiff), remTo.beforeEdit, addTo.afterEdit);

        return DiffNode.createCode(DiffType.NON, from, to, added.getText() /* equals removed.getText() */);
    }
}

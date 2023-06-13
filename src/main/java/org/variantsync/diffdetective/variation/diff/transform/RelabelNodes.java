package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;

import java.util.function.Function;

/**
 * Transformer that changes the label of each node using a relable function.
 * @author Paul Bittner
 */
public class RelabelNodes<L extends Label> implements DiffTreeTransformer<L> {
    private final Function<DiffNode<L>, L> getLabel;

    /**
     * Creates a new transformation that relables each node with the given function.
     * @param newLabelOfNode This function is invoked once for each DiffNode in the transformed
     *                       DiffTree. The returned String is set as the node's new label.
     */
    public RelabelNodes(final Function<DiffNode<L>, L> newLabelOfNode) {
        this.getLabel = newLabelOfNode;
    }

    /**
     * Creates a new transformation that sets the label of each node to the given string.
     * @param label The new label for each node in a DiffTree.
     */
    public RelabelNodes(final L label) {
        this(d -> label);
    }

    @Override
    public void transform(DiffTree<L> diffTree) {
        diffTree.forAll(d -> d.setLabel(getLabel.apply(d)));
    }
}

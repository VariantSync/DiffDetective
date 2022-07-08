package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.function.Function;

/**
 * Transformer that changes the label of each node using a relable function.
 * @author Paul Bittner
 */
public class RelabelNodes implements DiffTreeTransformer {
    private final Function<DiffNode, String> getLabel;

    /**
     * Creates a new transformation that relables each node with the given function.
     * @param newLabelOfNode This function is invoked once for each DiffNode in the transformed
     *                       DiffTree. The returned String is set as the node's new label.
     * @see DiffNode#setLabel(String)
     */
    public RelabelNodes(final Function<DiffNode, String> newLabelOfNode) {
        this.getLabel = newLabelOfNode;
    }

    /**
     * Creates a new transformation that sets the label of each node to the given string.
     * @param label The new label for each node in a DiffTree.
     */
    public RelabelNodes(final String label) {
        this(d -> label);
    }

    @Override
    public void transform(DiffTree diffTree) {
        diffTree.forAll(d -> d.setLabel(getLabel.apply(d)));
    }
}

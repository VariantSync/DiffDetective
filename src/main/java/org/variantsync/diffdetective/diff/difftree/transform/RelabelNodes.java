package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.function.Function;

public class RelabelNodes implements DiffTreeTransformer {
    private final Function<DiffNode, String> getLabel;

    public RelabelNodes(final Function<DiffNode, String> newLabelOfNode) {
        this.getLabel = newLabelOfNode;
    }

    public RelabelNodes(final String label) {
        this(d -> label);
    }

    @Override
    public void transform(DiffTree diffTree) {
        diffTree.forAll(d -> d.setLabel(getLabel.apply(d)));
    }
}

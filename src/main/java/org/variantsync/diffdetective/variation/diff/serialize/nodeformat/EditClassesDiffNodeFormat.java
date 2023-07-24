package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

public class EditClassesDiffNodeFormat<L extends Label> implements DiffNodeLabelFormat<L> {
    @Override
    public String toLabel(DiffNode<? extends L> node) {
        return ShowNodeFormat.toLabel(
                node,
                n -> ProposedEditClasses.Instance.match(node).getName()
        );
    }
}

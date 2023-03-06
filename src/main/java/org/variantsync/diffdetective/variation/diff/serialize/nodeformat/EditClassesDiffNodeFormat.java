package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.variation.diff.DiffNode;

public class EditClassesDiffNodeFormat implements DiffNodeLabelFormat {
    @Override
    public String toLabel(DiffNode node) {
        return ShowNodeFormat.toLabel(
                node,
                n -> ProposedEditClasses.Instance.match(node).getName()
        );
    }
}

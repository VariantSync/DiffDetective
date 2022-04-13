package org.variantsync.diffdetective.diff.difftree.serialize.edgeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.functjonal.Functjonal;

public class DefaultEdgeLabelFormat extends EdgeLabelFormat {
    public DefaultEdgeLabelFormat() {
        super();
    }

    public DefaultEdgeLabelFormat(final EdgeLabelFormat.Direction direction) {
        super(direction);
    }

    @Override
    public String edgeToLineGraph(DiffNode from, DiffNode to, String labelPrefix) {
        return Functjonal.unwords(LineGraphConstants.LG_EDGE, from.getID(), to.getID(), labelPrefix);
    }
}

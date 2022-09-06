package org.variantsync.diffdetective.diff.difftree.serialize.edgeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.serialize.StyledEdge;
import org.variantsync.functjonal.Functjonal;

/**
 * Default implementation of {@link EdgeLabelFormat}.
 * This format does not add any extra information to edge's labels.
 * @author Paul Bittner
 */
public class DefaultEdgeLabelFormat extends EdgeLabelFormat {
    /**
     * Creates a new default edge label format.
     */
    public DefaultEdgeLabelFormat() {
        super();
    }

    /**
     * Creates a new default edge label format that interprets edges as following the given direction.
     * @param direction Directions in which edge IO should be done.
     */
    public DefaultEdgeLabelFormat(final EdgeLabelFormat.Direction direction) {
        super(direction);
    }

    @Override
    public String edgeToLineGraph(DiffNode from, DiffNode to, String labelPrefix) {
        return Functjonal.unwords(LineGraphConstants.LG_EDGE, from.getID(), to.getID(), labelPrefix);
    }

    @Override
    public String labelOf(StyledEdge edge) {
        return "";
    }
}

package org.variantsync.diffdetective.variation.diff.serialize.edgeformat;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.serialize.StyledEdge;

/**
 * Default implementation of {@link EdgeLabelFormat}.
 * This format does not add any extra information to edge's labels.
 * @author Paul Bittner
 */
public class DefaultEdgeLabelFormat<L extends Label> extends EdgeLabelFormat<L> {
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
    public <La extends L> String labelOf(StyledEdge<La> edge) {
        return "";
    }
}

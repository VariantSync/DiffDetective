package org.variantsync.diffdetective.variation.diff.serialize.edgeformat;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

import org.variantsync.diffdetective.variation.diff.DiffTree; // For JavaDoc
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.serialize.StyledEdge;

/**
 * An edge format encoding the child index of this edge.
 * The child index is the index of the child to the parent of the edge. If there is no child parent
 * relationship between the children in a given edge, the child index is -1.
 *
 * This index is encoded into decimal and delimited by a semicolon from the previous value.
 *
 * This format is mainly useful to equivalence of two {@link DiffTree}s, for example in tests.
 *
 * @author Benjamin Moosherr
 */
public class ChildOrderEdgeFormat extends EdgeLabelFormat {
    @Override
    public String labelOf(StyledEdge edge) {
        int[] index = new int[2];
        Time.forAll(time -> {
            int i = edge.from().indexOfChild(edge.to(), time);
            int j = edge.to().indexOfChild(edge.from(), time);
            index[time.ordinal()] = i < 0 ? j : i;
        });

        return String.format(";%d,%d", index[BEFORE.ordinal()], index[AFTER.ordinal()]);
    }
}

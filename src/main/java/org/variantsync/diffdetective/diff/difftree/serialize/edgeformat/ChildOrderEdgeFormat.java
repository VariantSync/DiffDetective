package org.variantsync.diffdetective.diff.difftree.serialize.edgeformat;

import org.variantsync.diffdetective.diff.difftree.serialize.StyledEdge;
import org.variantsync.diffdetective.diff.difftree.DiffTree; // For JavaDoc

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
        int i = edge.from().indexOfChild(edge.to());
        int j = edge.to().indexOfChild(edge.from());
        return String.format(";%d", i < 0 ? j : i);
    }
}

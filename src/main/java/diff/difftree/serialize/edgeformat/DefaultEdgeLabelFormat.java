package diff.difftree.serialize.edgeformat;

import de.variantsync.functjonal.Functjonal;
import diff.difftree.DiffNode;
import diff.difftree.LineGraphConstants;

public class DefaultEdgeLabelFormat extends EdgeLabelFormat {
    @Override
    public String edgeToLineGraph(DiffNode from, DiffNode to, String labelPrefix) {
        return Functjonal.unwords(LineGraphConstants.LG_EDGE, from.getID(), to.getID(), labelPrefix);
    }
}

package mining.formats;

import diff.DiffLineNumber;
import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.functjonal.Pair;
import util.fide.FixTrueFalse;

public interface MiningNodeFormat extends DiffNodeLabelFormat {
    Pair<DiffType, CodeType> fromEncodedTypes(final String tag);

    default DiffNode fromLabelAndId(String lineGraphNodeLabel, int nodeId) {
        /// We cannot reuse the id as it is just a sequential integer. It thus, does not contain any information.
        final DiffLineNumber lineFrom = DiffLineNumber.Invalid();
        final DiffLineNumber lineTo = DiffLineNumber.Invalid();
        final String resultLabel = "";

        final Pair<DiffType, CodeType> types = fromEncodedTypes(lineGraphNodeLabel);
        if (types.second() == CodeType.CODE) {
            return DiffNode.createCode(types.first(),
                    lineFrom, lineTo, resultLabel);
        } else {
            return new DiffNode(types.first(), types.second(), lineFrom, lineTo, FixTrueFalse.True, resultLabel);
        }
    }
}

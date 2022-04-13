package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.CodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.functjonal.Pair;

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

package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.NodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.functjonal.Pair;

public interface MiningNodeFormat extends DiffNodeLabelFormat {
    Pair<DiffType, NodeType> fromEncodedTypes(final String tag);

    @Override
    default DiffNode fromLabelAndId(String lineGraphNodeLabel, int nodeId) {
        /// We cannot reuse the id as it is just a sequential integer. It thus, does not contain any information.
        final DiffLineNumber lineFrom = new DiffLineNumber(nodeId, nodeId, nodeId);
        final DiffLineNumber lineTo = new DiffLineNumber(nodeId, nodeId, nodeId);
        final String resultLabel = "";

        final Pair<DiffType, NodeType> types = fromEncodedTypes(lineGraphNodeLabel);
        lineFrom.as(types.first());
        lineTo.as(types.first());
        if (types.second() == NodeType.ARTIFACT) {
            return DiffNode.createArtifact(types.first(),
                    lineFrom, lineTo, resultLabel);
        } else {
            return new DiffNode(types.first(), types.second(), lineFrom, lineTo, FixTrueFalse.True, resultLabel);
        }
    }
}

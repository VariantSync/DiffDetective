package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.diff.difftree.NodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.functjonal.Pair;

import java.util.Arrays;

/**
 * Analogous to {@link ReleaseMiningDiffNodeFormat} but produces human readable labels instead of using integers.
 * Artifact nodes are labeled with the name of their matched elementary pattern.
 * Annotation nodes are labeled with DIFFTYPE_NODETYPE (e.g., an added IF node gets the label ADD_IF).
 */
public class DebugMiningDiffNodeFormat implements MiningNodeFormat {
	@Override
	public String toLabel(final DiffNode node) {
        if (node.isArtifact()) {
            return ProposedElementaryPatterns.Instance.match(node).getName();
        } else if (node.isRoot()) {
            return node.diffType + "_" + NodeType.IF;
        } else {
            return node.diffType + "_" + node.nodeType;
        }
	}

    @Override
    public Pair<DiffType, NodeType> fromEncodedTypes(String tag) {
        // If the label starts with ADD, REM, or NON
        if (Arrays.stream(DiffType.values()).anyMatch(diffType -> tag.startsWith(diffType.toString()))) {
            // then it is an annotation node
            final DiffType dt = DiffType.fromName(tag);
            final int nodeTypeBegin = tag.indexOf("_") + 1;
            final NodeType nt = NodeType.fromName(tag.substring(nodeTypeBegin));
            if (nt == NodeType.ROOT) {
                throw new IllegalArgumentException("There should be no roots in mined patterns!");
            }
            return new Pair<>(dt, nt);
        } else {
            final ElementaryPattern pattern = ProposedElementaryPatterns.Instance.fromName(tag).orElseThrow(
                    () -> new IllegalStateException("Label \"" + tag + "\" is neither an annotation label, nor an elementary pattern!")
            );

            return new Pair<>(pattern.getDiffType(), NodeType.ARTIFACT);
        }
    }
}

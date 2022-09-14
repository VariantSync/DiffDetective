package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.diff.difftree.NodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.functjonal.Pair;

/**
 * Formats for DiffNodes for mining.
 * The label of a node starts with c (for code) if it is an artifact node and with m (for macro) otherwise.
 * The label of artifact nodes is followed by the index of its matched elementary pattern.
 * The label of diff nodes is followed by the ordinal of its diff type and the ordinal of its node type.
 *
 * Examples:
 * DiffNode with nodeType=ARTIFACT and elementary pattern AddWithMapping gets the label "c1" because AddWithMapping has index 1.
 * DiffNode with nodeType=ELSE and difftype=REM gets the label "m23" because the ordinal or REM is 2 and the ordinal of ELSE is 3.
 */
public class ReleaseMiningDiffNodeFormat implements MiningNodeFormat {
    public final static String ARTIFACT_PREFIX = "c";
    public final static String ANNOTATION_PREFIX = "m";

    private static int toId(final ElementaryPattern p) {
        for (int i = 0; i < ProposedElementaryPatterns.All.size(); ++i) {
            if (p.equals(ProposedElementaryPatterns.All.get(i))) {
                return i;
            }
        }

        throw new IllegalArgumentException("bug");
    }

    private static ElementaryPattern fromId(int id) {
        return ProposedElementaryPatterns.All.get(id);
    }

    @Override
    public String toLabel(DiffNode node) {
        if (node.isArtifact()) {
            return ARTIFACT_PREFIX + toId(ProposedElementaryPatterns.Instance.match(node));
        } else {
            final NodeType nodeType = node.isRoot() ? NodeType.IF : node.nodeType;
            return ANNOTATION_PREFIX + node.diffType.ordinal() + nodeType.ordinal();
        }
    }

    @Override
    public Pair<DiffType, NodeType> fromEncodedTypes(String tag) {
        if (tag.startsWith(ARTIFACT_PREFIX)) {
            final ElementaryPattern pattern = fromId(Integer.parseInt(tag.substring(ARTIFACT_PREFIX.length())));
            return new Pair<>(pattern.getDiffType(), NodeType.ARTIFACT);
        } else {
            Assert.assertTrue(tag.startsWith(ANNOTATION_PREFIX));
            final int diffTypeBegin = ANNOTATION_PREFIX.length();
            final int nodeTypeBegin = diffTypeBegin + 1;
            final DiffType diffType = DiffType.values()[Integer.parseInt(
                    tag.substring(diffTypeBegin, nodeTypeBegin)
            )];
            final NodeType nodeType = NodeType.values()[Integer.parseInt(
                    tag.substring(nodeTypeBegin, nodeTypeBegin + 1)
            )];
            if (nodeType == NodeType.ROOT) {
                throw new IllegalArgumentException("There should be no roots in mined patterns!");
            }
            return new Pair<>(diffType, nodeType);
        }
    }
}

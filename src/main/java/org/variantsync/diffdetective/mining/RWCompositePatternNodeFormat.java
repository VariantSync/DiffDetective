package org.variantsync.diffdetective.mining;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.mining.formats.DebugMiningDiffNodeFormat;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;

public class RWCompositePatternNodeFormat extends DebugMiningDiffNodeFormat {
    @Override
    public String toLabel(final DiffNode node) {
        if (node.isArtifact()) {
            return ProposedElementaryPatterns.Instance.match(node).getName() + "<br>" + node.getLabel();
        } else {
            return node.diffType + "_" + switch (node.nodeType) {
                case ROOT -> "r";
                case IF -> "mapping<br> " + node.getLabel();
                case ELSE -> "else";
                case ELIF -> "elif<br>" + node.getLabel();
                default -> node.nodeType + "<br>" + node.getLabel();
            };
        }
    }
}

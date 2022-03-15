package mining;

import diff.difftree.DiffNode;
import mining.formats.DebugMiningDiffNodeFormat;
import pattern.atomic.proposed.ProposedAtomicPatterns;

public class RWCompositePatternNodeFormat extends DebugMiningDiffNodeFormat {
    @Override
    public String toLabel(final DiffNode node) {
        if (node.isCode()) {
            return ProposedAtomicPatterns.Instance.match(node).getName() + "<br>" + node.getLabel();
        } else {
            return node.diffType + "_" + switch (node.codeType) {
                case ROOT -> "r";
                case IF -> "mapping<br> " + node.getLabel();
                case ELSE -> "else";
                case ELIF -> "elif<br>" + node.getLabel();
                default -> node.codeType + "<br>" + node.getLabel();
            };
        }
    }
}

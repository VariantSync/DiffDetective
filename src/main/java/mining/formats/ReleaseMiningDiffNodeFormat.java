package mining.formats;

import diff.DiffLineNumber;
import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.prop4j.True;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Assert;

/**
 * Formats for DiffNodes for mining.
 * The label of a node starts with c if it is a code node and with m (for macro) otherwise.
 * The label of code nodes is followed by the index of its matched atomic pattern.
 * The label of diff nodes is followed by the ordinal of its diff type and the ordinal of its code type.
 *
 * Examples:
 * DiffNode with codeType=CODE and atomic pattern AddWithMapping gets the label "c1" because AddWithMapping has index 1.
 * DiffNode with codeType=ELSE and difftype=REM gets the label "m23" because the ordinal or REM is 2 and the ordinal of ELSE is 3.
 */
public class ReleaseMiningDiffNodeFormat implements DiffNodeLabelFormat {
    public final static String CODE_PREFIX = "c";
    public final static String MACRO_PREFIX = "m";

    private static int toId(final AtomicPattern p) {
        for (int i = 0; i < ProposedAtomicPatterns.All.size(); ++i) {
            if (p.equals(ProposedAtomicPatterns.All.get(i))) {
                return i;
            }
        }

        throw new IllegalArgumentException("bug");
    }

    private static AtomicPattern fromId(int id) {
        return ProposedAtomicPatterns.All.get(id);
    }

    @Override
    public DiffNode fromLabelAndId(String lineGraphNodeLabel, int nodeId) {
        /// We cannot reuse the id as it is just a sequential integer. It thus, does not contain any information.
        final DiffLineNumber lineFrom = DiffLineNumber.Invalid();
        final DiffLineNumber lineTo = DiffLineNumber.Invalid();
        final String resultLabel = "";

        if (lineGraphNodeLabel.startsWith(CODE_PREFIX)) {
            final AtomicPattern pattern = fromId(Integer.parseInt(lineGraphNodeLabel.substring(CODE_PREFIX.length())));
            return DiffNode.createCode(pattern.getDiffType(),
                    lineFrom, lineTo, resultLabel);
        } else {
            Assert.assertTrue(lineGraphNodeLabel.startsWith(MACRO_PREFIX));
            final int diffTypeBegin = MACRO_PREFIX.length();
            final int codeTypeBegin = diffTypeBegin + 1;
            final DiffType diffType = DiffType.values()[Integer.parseInt(
                    lineGraphNodeLabel.substring(diffTypeBegin, codeTypeBegin)
            )];
            final CodeType codeType = CodeType.values()[Integer.parseInt(
                    lineGraphNodeLabel.substring(codeTypeBegin, codeTypeBegin + 1)
            )];
            if (codeType == CodeType.ROOT) {
                throw new IllegalArgumentException("There should be no roots in mined patterns!");
            }
            return new DiffNode(diffType, codeType, lineFrom, lineTo, new True(), resultLabel);
        }
    }

    @Override
    public String toLabel(DiffNode node) {
        if (node.isCode()) {
            return CODE_PREFIX + toId(ProposedAtomicPatterns.Instance.match(node));
        } else {
            final CodeType codeType = node.isRoot() ? CodeType.IF : node.codeType;
            return MACRO_PREFIX + node.diffType.ordinal() + codeType.ordinal();
        }
    }
}

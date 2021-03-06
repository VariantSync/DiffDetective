package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.diff.difftree.CodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.functjonal.Pair;

/**
 * Formats for DiffNodes for mining.
 * The label of a node starts with c if it is a code node and with m (for macro) otherwise.
 * The label of code nodes is followed by the index of its matched elementary pattern.
 * The label of diff nodes is followed by the ordinal of its diff type and the ordinal of its code type.
 *
 * Examples:
 * DiffNode with codeType=CODE and elementary pattern AddWithMapping gets the label "c1" because AddWithMapping has index 1.
 * DiffNode with codeType=ELSE and difftype=REM gets the label "m23" because the ordinal or REM is 2 and the ordinal of ELSE is 3.
 */
public class ReleaseMiningDiffNodeFormat implements MiningNodeFormat {
    public final static String CODE_PREFIX = "c";
    public final static String MACRO_PREFIX = "m";

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
        if (node.isCode()) {
            return CODE_PREFIX + toId(ProposedElementaryPatterns.Instance.match(node));
        } else {
            final CodeType codeType = node.isRoot() ? CodeType.IF : node.codeType;
            return MACRO_PREFIX + node.diffType.ordinal() + codeType.ordinal();
        }
    }

    @Override
    public Pair<DiffType, CodeType> fromEncodedTypes(String tag) {
        if (tag.startsWith(CODE_PREFIX)) {
            final ElementaryPattern pattern = fromId(Integer.parseInt(tag.substring(CODE_PREFIX.length())));
            return new Pair<>(pattern.getDiffType(), CodeType.CODE);
        } else {
            Assert.assertTrue(tag.startsWith(MACRO_PREFIX));
            final int diffTypeBegin = MACRO_PREFIX.length();
            final int codeTypeBegin = diffTypeBegin + 1;
            final DiffType diffType = DiffType.values()[Integer.parseInt(
                    tag.substring(diffTypeBegin, codeTypeBegin)
            )];
            final CodeType codeType = CodeType.values()[Integer.parseInt(
                    tag.substring(codeTypeBegin, codeTypeBegin + 1)
            )];
            if (codeType == CodeType.ROOT) {
                throw new IllegalArgumentException("There should be no roots in mined patterns!");
            }
            return new Pair<>(diffType, codeType);
        }
    }
}

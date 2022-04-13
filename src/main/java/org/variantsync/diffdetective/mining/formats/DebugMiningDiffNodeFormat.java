package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.diff.difftree.CodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.atomic.AtomicPattern;
import org.variantsync.diffdetective.pattern.atomic.proposed.ProposedAtomicPatterns;
import org.variantsync.functjonal.Pair;

import java.util.Arrays;

/**
 * Analogous to {@link ReleaseMiningDiffNodeFormat} but produces human readable labels instead of using integers.
 * Code nodes are labeled with the name of their matched atomic pattern.
 * Macro nodes are labeled with DIFFTYPE_CODETYPE (e.g., an added IF node gets the label ADD_IF).
 */
public class DebugMiningDiffNodeFormat implements MiningNodeFormat {
	@Override
	public String toLabel(final DiffNode node) {
        if (node.isCode()) {
            return ProposedAtomicPatterns.Instance.match(node).getName();
        } else if (node.isRoot()) {
            return node.diffType + "_" + CodeType.IF;
        } else {
            return node.diffType + "_" + node.codeType;
        }
	}

    @Override
    public Pair<DiffType, CodeType> fromEncodedTypes(String tag) {
        // If the label starts with ADD, REM, or NON
        if (Arrays.stream(DiffType.values()).anyMatch(diffType -> tag.startsWith(diffType.toString()))) {
            // then it is a macro node
            final DiffType dt = DiffType.fromName(tag);
            final int codeTypeBegin = tag.indexOf("_") + 1;
            final CodeType ct = CodeType.fromName(tag.substring(codeTypeBegin));
            if (ct == CodeType.ROOT) {
                throw new IllegalArgumentException("There should be no roots in mined patterns!");
            }
            return new Pair<>(dt, ct);
        } else {
            final AtomicPattern pattern = ProposedAtomicPatterns.Instance.fromName(tag).orElseThrow(
                    () -> new IllegalStateException("Label \"" + tag + "\" is neither a macro label, nor an atomic pattern!")
            );

            return new Pair<>(pattern.getDiffType(), CodeType.CODE);
        }
    }
}

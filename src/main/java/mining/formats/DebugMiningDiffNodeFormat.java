package mining.formats;

import de.variantsync.functjonal.Product;
import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;

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
    public Product<DiffType, CodeType> fromEncodedTypes(String tag) {
        // If the label starts with ADD, REM, or NON
        if (Arrays.stream(DiffType.values()).anyMatch(diffType -> tag.startsWith(diffType.toString()))) {
            // then it is a macro node
            final DiffType dt = DiffType.fromName(tag);
            final int codeTypeBegin = tag.indexOf("_") + 1;
            final CodeType ct = CodeType.fromName(tag.substring(codeTypeBegin));
            if (ct == CodeType.ROOT) {
                throw new IllegalArgumentException("There should be no roots in mined patterns!");
            }
            return new Product<>(dt, ct);
        } else {
            final AtomicPattern pattern = ProposedAtomicPatterns.Instance.fromName(tag).orElseThrow(
                    () -> new IllegalStateException("Label \"" + tag + "\" is neither a macro label, nor an atomic pattern!")
            );

            return new Product<>(pattern.getDiffType(), CodeType.CODE);
        }
    }
}

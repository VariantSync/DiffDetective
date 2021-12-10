package main.mining;

import diff.DiffLineNumber;
import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.prop4j.True;
import pattern.AtomicPattern;

import java.util.Arrays;

/**
 * Analogous to {@link ReleaseMiningDiffNodeFormat} but produces human readable labels instead of using integers.
 * Code nodes are labeled with the name of their matched atomic pattern.
 * Macro nodes are labeled with DIFFTYPE_CODETYPE (e.g., an added IF node gets the label ADD_IF).
 */
public class DebugMiningDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public DiffNode fromLabelAndId(final String label, final int nodeId) {
        /// We cannot reuse the id as it is just a sequential integer. It thus, does not contain any information.
        final DiffLineNumber lineFrom = DiffLineNumber.Invalid();
        final DiffLineNumber lineTo = DiffLineNumber.Invalid();
        final String resultLabel = "";

        // If the label starts with ADD, REM, or NON
        if (Arrays.stream(DiffType.values()).anyMatch(diffType -> label.startsWith(diffType.toString()))) {
            // then it is a macro node
            final DiffType dt = DiffType.fromName(label);
            final int codeTypeBegin = label.indexOf("_") + 1;
            final CodeType ct = CodeType.fromName(label.substring(codeTypeBegin));
            if (ct == CodeType.ROOT) {
                throw new IllegalArgumentException("There should be no roots in mined patterns!");
            }
            return new DiffNode(dt, ct, lineFrom, lineTo, new True(), resultLabel);
        } else {
            // the label should describe a pattern
            final AtomicPattern pattern = AtomicPattern.fromName(label);
            if (pattern == null) {
                throw new IllegalStateException("Label \"" + label + "\" is neither a macro label, nor an atomic pattern!");
            }

            return DiffNode.createCode(pattern.getDiffType(),
                    lineFrom, lineTo, resultLabel);
        }
	}

	@Override
	public String toLabel(final DiffNode node) {
        if (node.isCode()) {
            return AtomicPattern.getPattern(node).getName();
        } else if (node.isRoot()) {
            return node.diffType + "_" + CodeType.IF;
        } else {
            return node.diffType + "_" + node.codeType;
        }
	}
}

package diff.difftree.serialize.nodeformat;

import diff.difftree.CodeType;
import diff.difftree.DiffNode;

/**
 * A concrete implementation for Mining of a node label.
 * Print metadata required for semantic pattern mining.
 */
public class MiningDiffNodeFormat implements DiffNodeLabelFormat {

	//TODO Paul implementiert Methode richtig
	@Override
	public DiffNode fromLabelAndId(final String lineGraphNodeLabel, final int nodeId) {
//		String[] vertex = lineGraphNodeLine.split(" ");
//		String nodeId = vertex[1];
//		String editPattern = vertex[2];
//
//		String nodeLabel; 
//		DiffType diffType = DiffType.ofDiffLine(editPattern);
//		if (editPattern.matches(DiffType.ADD + "_*")) {
//			nodeLabel = editPattern.substring(DiffType.ADD.toString().length() + 1, editPattern.length() - 1);
//		}
//		else if (editPattern.matches(DiffType.REM + "_*")) {
//			nodeLabel = editPattern.substring(DiffType.ADD.toString().length() + 1, editPattern.length() - 1);
//		}
//		else if (editPattern.matches(DiffType.NON + "_*")) {
//			nodeLabel = editPattern.substring(DiffType.ADD.toString().length() + 1, editPattern.length() - 1);
//		}
//		else nodeLabel = editPattern;
//		
//		CodeType codeType = CodeType.ofDiffLine(lineGraphNodeLine);
//
//		// missing: fromLines, toLines, featureMapping
//		return new DiffNode(diffType, codeType, null, null, null, nodeLabel);
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public String toLabel(final DiffNode node) {
		return (node.codeType == CodeType.CODE) ? node.getLabel() : node.diffType + "_" + node.getLabel();
	}

}
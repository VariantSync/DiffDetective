package diff.difftree.serialize.nodelabel;

import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;

/**
 * A concrete implementation for Mining of a {@link DiffTreeLineGraphImportOptions.NodeStyle}.
 * Print metadata required for semantic pattern mining.
 */
public class MiningDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	/**
	 *  TODO Paul implementiert Methode richtig
	 */
	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLine) {
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
	public String writeNodeToLineGraph(final DiffNode node) {
		return "v " + node.getID() + " " + ((node.codeType == CodeType.CODE) ? node.getLabel() : node.diffType + "_" + node.getLabel());
	}

}
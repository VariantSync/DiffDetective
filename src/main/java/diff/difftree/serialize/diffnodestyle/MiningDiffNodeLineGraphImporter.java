package diff.difftree.serialize;

import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;

/**
 * For mining pattern of {@link diff.difftree.serialize.DiffTreeNodeLabelFormat.NodePrintStyle NodePrintStyle} only.
 */
public class MiningDiffNodeLineGraphImporter implements DiffNodeLineGraphImporter {

	// TODO Paul implementiert Methode richtig
	@Override
	public DiffNode parse(String lineGraphLine) {

		String[] vertex = lineGraphLine.split(" ");
		String nodeId = vertex[1];
		String editPattern = vertex[2];

		// for Mining pattern only
		String nodeLabel; 
		DiffType diffType = DiffType.ofDiffLine(editPattern);
		if (editPattern.matches(DiffType.ADD + "_*")) {
			nodeLabel = editPattern.substring(DiffType.ADD.toString().length() + 1, editPattern.length() - 1);
		}
		else if (editPattern.matches(DiffType.REM + "_*")) {
			nodeLabel = editPattern.substring(DiffType.ADD.toString().length() + 1, editPattern.length() - 1);
		}
		else if (editPattern.matches(DiffType.NON + "_*")) {
			nodeLabel = editPattern.substring(DiffType.ADD.toString().length() + 1, editPattern.length() - 1);
		}
		else nodeLabel = editPattern;
		
		CodeType codeType = CodeType.ofDiffLine(lineGraphLine);

		// missing: fromLines, toLines, featureMapping
		return new DiffNode(diffType, codeType, null, null, null, nodeLabel);
	}

}

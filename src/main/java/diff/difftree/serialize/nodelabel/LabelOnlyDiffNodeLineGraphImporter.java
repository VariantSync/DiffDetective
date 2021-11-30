package diff.difftree.serialize.nodelabel;

import diff.difftree.DiffNode;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;

/**
 * A concrete implementation for LabelOnly of a {@link DiffTreeLineGraphImportOptions.NodeStyle}.
 * Print only the label.
 */
public class LabelOnlyDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLine) {
		throw new RuntimeException("Node style ‘LABELONLY’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return "v " + node.getID() + " " + node.getLabel();
	}

}
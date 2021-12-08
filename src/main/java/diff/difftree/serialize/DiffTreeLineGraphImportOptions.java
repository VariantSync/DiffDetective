package diff.difftree.serialize;

import diff.difftree.DiffTree;
import diff.difftree.serialize.nodeformat.DiffTreeNodeLabelFormat;
import diff.difftree.serialize.treeformat.DiffTreeLabelFormat;

/**
 * Options necessary for importing a line graph.
 * This records contains information for importing a {@link DiffTree} from a line graph, such as the graph format and tree and node layouts.
 */
public record DiffTreeLineGraphImportOptions(GraphFormat format, 
		DiffTreeLabelFormat treeParser, 
		DiffTreeNodeLabelFormat nodeParser) { }
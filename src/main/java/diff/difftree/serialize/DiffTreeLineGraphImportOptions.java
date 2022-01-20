package diff.difftree.serialize;

import diff.difftree.DiffTree;
import diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import diff.difftree.serialize.treeformat.DiffTreeLabelFormat;

/**
 * Options necessary for importing a line graph.
 * This records contains information for importing a {@link DiffTree} from a line graph, such as the graph format and tree and node layouts.
 */
public record DiffTreeLineGraphImportOptions(
        GraphFormat graphFormat,
        DiffTreeLabelFormat treeFormat,
        DiffNodeLabelFormat nodeFormat,
        EdgeLabelFormat edgeFormat) { }
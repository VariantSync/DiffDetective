package diff.difftree.serialize;

import diff.difftree.serialize.nodeformat.DiffTreeNodeLabelFormat;
import diff.difftree.serialize.treeformat.DiffTreeLabelFormat;

/**
 * Options necessary for importing a line graph.
 */
public record DiffTreeLineGraphImportOptions(GraphFormat format, DiffTreeLabelFormat treeParser, DiffTreeNodeLabelFormat nodeParser) {}
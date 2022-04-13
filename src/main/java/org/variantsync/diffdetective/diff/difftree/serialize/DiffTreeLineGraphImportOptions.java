package org.variantsync.diffdetective.diff.difftree.serialize;

import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.DiffTreeLabelFormat;

/**
 * Options necessary for importing a line graph.
 * This records contains information for importing a {@link DiffTree} from a line graph, such as the graph format and tree and node layouts.
 */
public record DiffTreeLineGraphImportOptions(
        GraphFormat graphFormat,
        DiffTreeLabelFormat treeFormat,
        DiffNodeLabelFormat nodeFormat,
        EdgeLabelFormat edgeFormat) { }
package org.variantsync.diffdetective.variation.diff.serialize;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.DiffTreeLabelFormat;

/**
 * Options necessary for importing a line graph.
 * This records contains information for importing a {@link DiffTree} from a line graph, such as the graph format and tree and node layouts.
 * @param graphFormat {@link GraphFormat}
 * @param treeFormat {@link DiffTreeLabelFormat}
 * @param nodeFormat {@link DiffNodeLabelFormat}
 * @param edgeFormat {@link EdgeLabelFormat}
 */
public record LineGraphImportOptions<L extends Label>(
        GraphFormat graphFormat,
        DiffTreeLabelFormat treeFormat,
        DiffNodeLabelFormat<? super L> nodeFormat,
        EdgeLabelFormat<? super L> edgeFormat) { }

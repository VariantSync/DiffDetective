package org.variantsync.diffdetective.variation.diff.serialize;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.VariationDiffLabelFormat;

/**
 * Options necessary for importing a line graph.
 * This records contains information for importing a {@link VariationDiff} from a line graph, such as the graph format and tree and node layouts.
 * @param graphFormat {@link GraphFormat}
 * @param treeFormat {@link VariationDiffLabelFormat}
 * @param nodeFormat {@link DiffNodeLabelFormat}
 * @param edgeFormat {@link EdgeLabelFormat}
 */
public record LineGraphImportOptions<L extends Label>(
        GraphFormat graphFormat,
        VariationDiffLabelFormat treeFormat,
        DiffNodeLabelFormat<? super L> nodeFormat,
        EdgeLabelFormat<? super L> edgeFormat) { }

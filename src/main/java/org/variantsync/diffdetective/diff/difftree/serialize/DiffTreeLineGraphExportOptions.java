package org.variantsync.diffdetective.diff.difftree.serialize;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.render.PatchDiffRenderer;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.DiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Options necessary for exporting a line graph.
 * This records contains information for exporting a {@link DiffTree} into a line graph, such as the graph format and tree and node layouts.
 */
public record DiffTreeLineGraphExportOptions(
        GraphFormat graphFormat,
		DiffTreeLabelFormat treeFormat,
		DiffNodeLabelFormat nodeFormat,
        EdgeLabelFormat edgeFormat,
		ExplainedFilter<DiffTree> treeFilter,
        List<DiffTreeTransformer> treePreProcessing,
        BiConsumer<PatchDiff, Exception> onError) {
	
    public DiffTreeLineGraphExportOptions(GraphFormat graphFormat, DiffTreeLabelFormat treeFormat, DiffNodeLabelFormat nodeFormat, EdgeLabelFormat edgeFormat) {
        this(graphFormat, treeFormat, nodeFormat, edgeFormat, ExplainedFilter.Any(), new ArrayList<>(), LogError());
    }

    public DiffTreeLineGraphExportOptions(final DiffTreeLineGraphImportOptions importOptions) {
        this(
                importOptions.graphFormat(),
                importOptions.treeFormat(),
                importOptions.nodeFormat(),
                importOptions.edgeFormat()
        );
    }

    public static BiConsumer<PatchDiff, Exception> LogError() {
        return (p, e) -> Logger.error(e);
    }

    public static BiConsumer<PatchDiff, Exception> RenderError() {
        final PatchDiffRenderer errorRenderer = PatchDiffRenderer.ErrorRendering(DiffTreeRenderer.WithinDiffDetective());
        return (p, e) -> {
            Logger.error(e);
            Logger.error("Rendering patch");
            errorRenderer.render(p, Path.of("error"));
        };
    }

    public static BiConsumer<PatchDiff, Exception> SysExitOnError() {
        return (p, e) -> System.exit(0);
    }
    
}
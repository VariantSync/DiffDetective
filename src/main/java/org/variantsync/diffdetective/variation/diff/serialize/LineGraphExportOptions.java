package org.variantsync.diffdetective.variation.diff.serialize;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.render.DiffTreeRenderer;
import org.variantsync.diffdetective.variation.diff.render.PatchDiffRenderer;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.DiffTreeLabelFormat;
import org.variantsync.diffdetective.variation.diff.transform.DiffTreeTransformer;

import java.nio.file.Path;
import java.util.function.BiConsumer;

/**
 * Options necessary for exporting a line graph.
 * This records contains information for exporting a {@link DiffTree} into a line graph, such as the graph format and tree and node layouts.
 * @param graphFormat {@link GraphFormat}
 * @param treeFormat {@link DiffTreeLabelFormat}
 * @param nodeFormat {@link DiffNodeLabelFormat}
 * @param edgeFormat {@link EdgeLabelFormat}
 * @param onError Callback that is invoked when an error occurs.
 * @author Paul Bittner
 */
public record LineGraphExportOptions(
        GraphFormat graphFormat,
		DiffTreeLabelFormat treeFormat,
		DiffNodeLabelFormat nodeFormat,
        EdgeLabelFormat edgeFormat,
        BiConsumer<PatchDiff, Exception> onError) {

    /**
     * Creates a export options with a neutral filter (that accepts all trees), no transformers, and that logs errors.
     */
    public LineGraphExportOptions(GraphFormat graphFormat, DiffTreeLabelFormat treeFormat, DiffNodeLabelFormat nodeFormat, EdgeLabelFormat edgeFormat) {
        this(graphFormat, treeFormat, nodeFormat, edgeFormat, LogError());
    }

    /**
     * Create export options from the given import options.
     * Invokes {@link LineGraphExportOptions#LineGraphExportOptions(GraphFormat, DiffTreeLabelFormat, DiffNodeLabelFormat, EdgeLabelFormat)}
     * with all formats from the given import options.
     * @param importOptions The import options to convert to export options.
     */
    public LineGraphExportOptions(final LineGraphImportOptions importOptions) {
        this(
                importOptions.graphFormat(),
                importOptions.treeFormat(),
                importOptions.nodeFormat(),
                importOptions.edgeFormat()
        );
    }

    /**
     * Default value for {@link #onError} that logs errors.
     */
    public static BiConsumer<PatchDiff, Exception> LogError() {
        return (p, e) -> Logger.error(e);
    }

    /**
     * Default value for {@link #onError} that renders errors with {@link PatchDiffRenderer#ErrorRendering} (with {@link DiffTreeRenderer#WithinDiffDetective()}.
     */
    public static BiConsumer<PatchDiff, Exception> RenderError() {
        final PatchDiffRenderer errorRenderer = PatchDiffRenderer.ErrorRendering(DiffTreeRenderer.WithinDiffDetective());
        return (p, e) -> {
            Logger.error(e, "Rendering patch");
            errorRenderer.render(p, Path.of("error"));
        };
    }

    /**
     * Default value for {@link #onError} that exits the program immediately upon an error with {@link System#exit(int)}.
     */
    public static BiConsumer<PatchDiff, Exception> SysExitOnError() {
        return (p, e) -> System.exit(1);
    }
}

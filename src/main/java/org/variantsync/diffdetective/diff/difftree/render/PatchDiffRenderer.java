package org.variantsync.diffdetective.diff.difftree.render;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.GitPatch;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.TypeDiffNodeFormat;
import org.variantsync.diffdetective.util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;

/**
 * A wrapper for a {@link DiffTreeRenderer} for rendering {@link PatchDiff}s.
 * Next to rendering, a PatchDiffRenderer also writes the diff of a given patch
 * to a file at the same directory.
 * A PatchDiffRenderer also provides facilities for debug rendering upon errors.
 * @author Paul Bittner
 */
public class PatchDiffRenderer {
    /**
     * Default RenderOptions for debug rendering of DiffTrees
     * relevant to the occurrence of an error.
     */
    public static final RenderOptions ErrorDiffTreeRenderOptions = new RenderOptions.Builder()
//            .setNodeFormat(new MappingsDiffNodeFormat())
            .setNodeFormat(new TypeDiffNodeFormat())
            .setDpi(2000)
    		.setNodesize(RenderOptions.DEFAULT.nodesize()/30)
    		.setEdgesize(0.2*RenderOptions.DEFAULT.edgesize())
    		.setArrowsize(RenderOptions.DEFAULT.arrowsize()/5)
    		.setFontsize(2)
    		.build();

    private final DiffTreeRenderer renderer;
    private final RenderOptions options;

    /**
     * Creates a PatchDiffRenderer wrapping the given renderer and rendering patches
     * with the given options.
     * @param renderer The renderer to use when rendering PatchDiffs.
     * @param options Options to use for all render calls.
     */
    public PatchDiffRenderer(final DiffTreeRenderer renderer, RenderOptions options) {
        this.renderer = renderer;
        this.options = options;
    }

    /**
     * Creates a new PatchDiffRenderer for rendering patches relevant to the occurrence
     * of an error. This method and the returned renderer are mainly intended to be used
     * for debugging.
     * @param renderer The renderer to use for error rendering of PatchDiffs.
     * @return A PatchDiffRenderer that may be used for debug error renderring.
     */
    public static PatchDiffRenderer ErrorRendering(final DiffTreeRenderer renderer) {
        return new PatchDiffRenderer(renderer, ErrorDiffTreeRenderOptions);
    }

    /**
     * Renders the given patch to the given output directory.
     * @param patch The patch to render.
     * @param outputDirectory The directory to which the rendered image should be written.
     * @see DiffTreeRenderer#render(DiffTree, String, Path, RenderOptions, LineGraphExportOptions, BiFunction)
     */
    public void render(final PatchDiff patch, final Path outputDirectory) {
        render(patch.getDiffTree(), patch, outputDirectory);
    }

    /**
     * Renders the given DiffTree that originated from the given patch to the given output directory.
     * @param diffTree The tree to render.
     * @param patch The patch from which the given tree was created.
     * @param outputDirectory The directory to which the rendered image should be written.
     */
    public void render(final DiffTree diffTree, final GitPatch patch, final Path outputDirectory) {
        renderer.render(diffTree, patch, outputDirectory, options);
        try {
            IO.write(outputDirectory.resolve(
                    patch.getFileName() + LineGraphConstants.TREE_NAME_SEPARATOR + patch.getCommitHash() + ".diff"
                    ),
                    patch.getDiff());
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}

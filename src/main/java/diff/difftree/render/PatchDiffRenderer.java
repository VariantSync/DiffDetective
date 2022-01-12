package diff.difftree.render;

import diff.PatchDiff;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.pmw.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PatchDiffRenderer {
    public static final DiffTreeRenderer.RenderOptions ErrorDiffTreeRenderOptions = new DiffTreeRenderer.RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new DebugDiffNodeFormat(),
            true,
            1000,
            DiffTreeRenderer.RenderOptions.DEFAULT.nodesize()/3,
            0.5*DiffTreeRenderer.RenderOptions.DEFAULT.edgesize(),
            DiffTreeRenderer.RenderOptions.DEFAULT.arrowsize()/2,
            2,
            true,
            List.of()
    );

    private final DiffTreeRenderer renderer;
    private final DiffTreeRenderer.RenderOptions options;

    public PatchDiffRenderer(final DiffTreeRenderer renderer, DiffTreeRenderer.RenderOptions options) {
        this.renderer = renderer;
        this.options = options;
    }

    public static PatchDiffRenderer ErrorRendering(final DiffTreeRenderer renderer) {
        return new PatchDiffRenderer(renderer, ErrorDiffTreeRenderOptions);
    }

    public void render(final PatchDiff patch, final Path outputDirectory) {
        renderer.render(patch, outputDirectory, options);
        try {
            IO.write(outputDirectory.resolve(patch.getFileName() + ".diff"), patch.getDiff());
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}

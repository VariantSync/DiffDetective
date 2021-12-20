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

    private final Path outDirectory;
    private final DiffTreeRenderer renderer;
    private final DiffTreeRenderer.RenderOptions options;

    public PatchDiffRenderer(final Path outDirectory, final DiffTreeRenderer renderer, DiffTreeRenderer.RenderOptions options) {
        this.outDirectory = outDirectory;
        this.renderer = renderer;
        this.options = options;
    }

    public static PatchDiffRenderer ErrorRendering(final DiffTreeRenderer renderer) {
        return new PatchDiffRenderer(Path.of("error"), renderer, ErrorDiffTreeRenderOptions);
    }

    public void render(final PatchDiff patch) {
        renderer.render(patch, outDirectory, options);
        try {
            IO.write(outDirectory.resolve(patch.getFileName() + ".diff"), patch.getFullDiff());
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}

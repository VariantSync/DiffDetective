package diff.difftree.render;

import diff.PatchDiff;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.nodeformat.DebugDiffNodeLineGraphImporter;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import java.io.IOException;
import java.nio.file.Path;
import org.pmw.tinylog.Logger;
import util.IO;

public class ErrorRendering {
    public static final DiffTreeRenderer.RenderOptions ErrorDiffTreeRenderOptions = new DiffTreeRenderer.RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new DebugDiffNodeLineGraphImporter(),
            true,
            1000,
            DiffTreeRenderer.RenderOptions.DEFAULT.nodesize()/3,
            0.5*DiffTreeRenderer.RenderOptions.DEFAULT.edgesize(),
            DiffTreeRenderer.RenderOptions.DEFAULT.arrowsize()/2,
            2,
            true
    );

    private final Path errorDirectory;
    private final DiffTreeRenderer renderer;
    private final DiffTreeRenderer.RenderOptions options;

    public ErrorRendering(final Path errorDirectory, final DiffTreeRenderer renderer, DiffTreeRenderer.RenderOptions options) {
        this.errorDirectory = errorDirectory;
        this.renderer = renderer;
        this.options = options;
    }

    public ErrorRendering(final DiffTreeRenderer renderer) {
        this(Path.of("error"), renderer, ErrorDiffTreeRenderOptions);
    }

    public void onError(final PatchDiff patch) {
        renderer.render(patch, errorDirectory, options);
        try {
            IO.write(errorDirectory.resolve(patch.getFileName() + ".diff"), patch.getFullDiff());
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}

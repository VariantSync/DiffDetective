package diff.difftree.render;

import diff.GitPatch;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.LineGraphConstants;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import mining.formats.DebugMiningDiffNodeFormat;
import org.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PatchDiffRenderer {
    public static final DiffTreeRenderer.RenderOptions ErrorDiffTreeRenderOptions = new DiffTreeRenderer.RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new MappingsDiffNodeFormat(),
            new DefaultEdgeLabelFormat(),
            false,
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
        render(patch.getDiffTree(), patch, outputDirectory);
    }

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

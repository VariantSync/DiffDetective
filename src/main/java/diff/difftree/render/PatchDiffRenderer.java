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

public class PatchDiffRenderer {
    public static final RenderOptions ErrorDiffTreeRenderOptions = new RenderOptions.Builder()
            .setNodeFormat(new MappingsDiffNodeFormat())
            .setDpi(1000)
    		.setNodesize(RenderOptions.DEFAULT.nodesize()/3)
    		.setEdgesize(0.5*RenderOptions.DEFAULT.edgesize())
    		.setArrowsize(RenderOptions.DEFAULT.arrowsize()/2)
    		.setFontsize(2)
    		.build();

    private final DiffTreeRenderer renderer;
    private final RenderOptions options;

    public PatchDiffRenderer(final DiffTreeRenderer renderer, RenderOptions options) {
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

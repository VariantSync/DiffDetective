package diff.difftree.render;

import diff.GitPatch;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.LineGraphConstants;
import diff.difftree.serialize.nodeformat.TypeDiffNodeFormat;
import org.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;

public class PatchDiffRenderer {
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

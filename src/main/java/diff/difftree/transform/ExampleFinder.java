package diff.difftree.transform;

import diff.GitPatch;
import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.render.PatchDiffRenderer;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.tinylog.Logger;
import util.Assert;
import util.IO;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ExampleFinder implements DiffTreeTransformer {
    public static final DiffTreeRenderer.RenderOptions ExportOptions = new DiffTreeRenderer.RenderOptions(
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

    private final Function<DiffTree, Optional<DiffTree>> isGoodExample;
    private final PatchDiffRenderer exampleExport;
    private final Path outputDir;

    public ExampleFinder(final Function<DiffTree, Optional<DiffTree>> isGoodExample, final Path outDir, DiffTreeRenderer renderer) {
        this.isGoodExample = isGoodExample;
        this.exampleExport = new PatchDiffRenderer(renderer, ExportOptions);
        this.outputDir = outDir;
    }

    @Override
    public void transform(DiffTree diffTree) {
        isGoodExample.apply(diffTree).ifPresent(this::exportExample);
    }

    private void exportExample(final DiffTree example) {
        Assert.assertTrue(example.getSource() instanceof GitPatch);
        final GitPatch patch = (GitPatch) example.getSource();
        final Path treeDir = outputDir.resolve(Path.of(patch.getCommitHash()));

        Logger.info("Exporting example candidate: " + patch);
        exampleExport.render(example, patch, treeDir);

        String metadata = "";
        metadata += "Child commit: " + patch.getCommitHash() + "\n";
        metadata += "Parent commit: " + patch.getParentCommitHash() + "\n";
        metadata += "File: " + patch.getFileName() + "\n";
        IO.tryWrite(treeDir.resolve(patch.getFileName() + ".metadata.txt"), metadata);
    }
}

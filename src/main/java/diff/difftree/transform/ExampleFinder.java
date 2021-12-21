package diff.difftree.transform;

import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.render.PatchDiffRenderer;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.pmw.tinylog.Logger;
import util.Assert;
import util.IO;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public class ExampleFinder implements DiffTreeTransformer {
    public static final DiffTreeRenderer.RenderOptions ExportOptions = new DiffTreeRenderer.RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new MappingsDiffNodeFormat(),
            false,
            1000,
            DiffTreeRenderer.RenderOptions.DEFAULT.nodesize()/3,
            0.5*DiffTreeRenderer.RenderOptions.DEFAULT.edgesize(),
            DiffTreeRenderer.RenderOptions.DEFAULT.arrowsize()/2,
            2,
            true,
            List.of()
    );

    private final Predicate<DiffTree> isGoodExample;
    private final PatchDiffRenderer exampleExport;
    private final Path outputDir;

    public ExampleFinder(final Predicate<DiffTree> isGoodExample, final Path outDir, DiffTreeRenderer renderer) {
        this.isGoodExample = isGoodExample;
        this.exampleExport = new PatchDiffRenderer(outDir, renderer, ExportOptions);
        this.outputDir = outDir;
    }

    @Override
    public void transform(DiffTree diffTree) {
        if (isGoodExample.test(diffTree)) {
            Assert.assertTrue(diffTree.getSource() instanceof PatchDiff);
            final PatchDiff patch = (PatchDiff) diffTree.getSource();
            Logger.info("Exporting example candidate: " + patch);
            exampleExport.render(patch);

            String metadata = "";
            metadata += "Child commit: " + patch.getCommitDiff().getCommitHash() + "\n";
            metadata += "Parent commit: " + patch.getCommitDiff().getParentCommitHash() + "\n";
            metadata += "File: " + patch.getFileName() + "\n";
            IO.tryWrite(outputDir.resolve(patch.getFileName() + ".metadata.txt"), metadata);
        }
    }
}

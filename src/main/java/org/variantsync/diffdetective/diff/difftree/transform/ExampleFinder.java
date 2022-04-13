package org.variantsync.diffdetective.diff.difftree.transform;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.GitPatch;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.render.PatchDiffRenderer;
import org.variantsync.diffdetective.diff.difftree.render.RenderOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.IO;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ExampleFinder implements DiffTreeTransformer {
    public static final RenderOptions ExportOptions = new RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new MappingsDiffNodeFormat(),
            new DefaultEdgeLabelFormat(),
            false,
            1000,
            RenderOptions.DEFAULT.nodesize()/3,
            0.5*RenderOptions.DEFAULT.edgesize(),
            RenderOptions.DEFAULT.arrowsize()/2,
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

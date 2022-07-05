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

/**
 * Helper class to find suitable running examples.
 * An example finder inspects each DiffTree, checks some requirements relevant for the
 * desired running example and writes candidates to a file and renders them.
 * An example finder should be side-effect free (i.e., it does not alter or transform the given DiffTree, just observes it).
 */
public class ExampleFinder implements DiffTreeTransformer {
    /**
     * Default render options for exporting example candidates.
     */
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

    /**
     * Creates a new ExampleFinder.
     * @param isGoodExample Function that decides whether a DiffTree is an example candidate or not.
     *                      Should return {@link Optional#empty()} when the given tree is not a good example and thus, should not be considered.
     *                      Should return a Difftree when the given tree is a good example candidate and should be exported.
     *                      The returned DiffTree might be the exact same DiffTree or a subtree (e.g., to only export a certain subtree that is relevant).
     * @param outDir The directory to which all example candidates should be written and rendered to.
     * @param renderer The renderer to use for rendering example candidates.
     */
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

        Logger.info("Exporting example candidate: {}", patch);
        exampleExport.render(example, patch, treeDir);

        String metadata = "";
        metadata += "Child commit: " + patch.getCommitHash() + "\n";
        metadata += "Parent commit: " + patch.getParentCommitHash() + "\n";
        metadata += "File: " + patch.getFileName() + "\n";
        IO.tryWrite(treeDir.resolve(patch.getFileName() + ".metadata.txt"), metadata);
    }
}

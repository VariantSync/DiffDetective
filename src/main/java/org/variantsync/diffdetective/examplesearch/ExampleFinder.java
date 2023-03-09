package org.variantsync.diffdetective.examplesearch;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.GitPatch;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.diff.text.TextBasedDiff;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.render.DiffTreeRenderer;
import org.variantsync.diffdetective.variation.diff.render.PatchDiffRenderer;
import org.variantsync.diffdetective.variation.diff.render.RenderOptions;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.MappingsDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Helper class to find suitable running examples.
 * An example finder inspects each DiffTree, checks some requirements relevant for the
 * desired running example and writes candidates to a file and renders them.
 * An example finder should be side-effect free (i.e., it does not alter or transform the given DiffTree, just observes it).
 */
public class ExampleFinder implements Analysis.Hooks {
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

    private final ExplainedFilter<DiffTree> isGoodExample;
    private final PatchDiffRenderer exampleExport;

    /**
     * Creates a new ExampleFinder.
     * @param isGoodExample Function that decides whether a DiffTree is an example candidate or not.
     *                      Should return {@link Optional#empty()} when the given tree is not a good example and thus, should not be considered.
     *                      Should return a Difftree when the given tree is a good example candidate and should be exported.
     *                      The returned DiffTree might be the exact same DiffTree or a subtree (e.g., to only export a certain subtree that is relevant).
     * @param renderer The renderer to use for rendering example candidates.
     */
    public ExampleFinder(final ExplainedFilter<DiffTree> isGoodExample, DiffTreeRenderer renderer) {
        this.isGoodExample = isGoodExample;
        this.exampleExport = new PatchDiffRenderer(renderer, ExportOptions);
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
        final Repository currentRepo = analysis.getRepository();
        final DiffTree diffTree = analysis.getCurrentDiffTree();
        final CPPAnnotationParser annotationParser = analysis.getRepository().getParseOptions().diffTreeParseOptions().annotationParser();

        // We do not want a difftree for the entire file but only for the local change to have a small example.
        final DiffTree localTree;
        try {
            final String localDiff = getDiff(diffTree);
            localTree = DiffTree.fromDiff(localDiff, new DiffTreeParseOptions(annotationParser, true, true));
            // Not every local diff can be parsed to a difftree because diffs are unaware of the underlying language (i.e., CPP).
            // We want only running examples whose diffs describe entire diff trees for easier understanding.
            if (isGoodExample.test(localTree)) {
                Assert.assertTrue(diffTree.getSource() instanceof GitPatch);
                final GitPatch diffTreeSource = (GitPatch) diffTree.getSource();
                localTree.setSource(diffTreeSource.shallowClone());
            } else {
                return false;
            }
        } catch (DiffParseException e) {
            return false;
        }

        exportExample(
                localTree,
                analysis.getOutputDir().resolve(currentRepo.getRepositoryName())
        );

        return true;
    }

    private void exportExample(final DiffTree example, final Path outputDir) {
        Assert.assertTrue(example.getSource() instanceof GitPatch);
        final GitPatch patch = (GitPatch) example.getSource();
        final Path treeDir = outputDir.resolve(Path.of(patch.getCommitHash()));

        Logger.info("Exporting example candidate: {}", patch);
        //exampleExport.render(example, patch, treeDir);

        try {
            Show.diff(example).dontShowButRenderToTexture().saveAsPng(treeDir.resolve(patch.getFileName() + ".png").toFile());
        } catch (IOException e) {
            Logger.error("Could not render example");
        }

        String metadata = "";
        metadata += "Child commit: " + patch.getCommitHash() + "\n";
        metadata += "Parent commit: " + patch.getParentCommitHash() + "\n";
        metadata += "File: " + patch.getFileName() + "\n";
        IO.tryWrite(treeDir.resolve(patch.getFileName() + ".metadata.txt"), metadata);
    }

    static String getDiff(final DiffTree tree) {
        final DiffTreeSource source = tree.getSource();
        Assert.assertTrue(source instanceof TextBasedDiff);
        return ((TextBasedDiff) source).getDiff();
    }
}

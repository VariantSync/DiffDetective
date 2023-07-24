package org.variantsync.diffdetective.examplesearch;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.GitPatch;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.diff.text.TextBasedDiff;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.render.VariationDiffRenderer;
import org.variantsync.diffdetective.variation.diff.render.PatchDiffRenderer;
import org.variantsync.diffdetective.variation.diff.render.RenderOptions;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.MappingsDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Helper class to find suitable running examples.
 * An example finder inspects each VariationDiff, checks some requirements relevant for the
 * desired running example and writes candidates to a file and renders them.
 * An example finder should be side-effect free (i.e., it does not alter or transform the given VariationDiff, just observes it).
 */
public class ExampleFinder implements Analysis.Hooks {
    /**
     * Default render options for exporting example candidates.
     */
    public static final RenderOptions<DiffLinesLabel> ExportOptions = new RenderOptions<>(
            GraphFormat.VARIATION_DIFF,
            new CommitDiffVariationDiffLabelFormat(),
            new MappingsDiffNodeFormat<>(),
            new DefaultEdgeLabelFormat<>(),
            false,
            1000,
            RenderOptions.DEFAULT().nodesize()/3,
            0.5*RenderOptions.DEFAULT().edgesize(),
            RenderOptions.DEFAULT().arrowsize()/2,
            2,
            true,
            List.of()
    );

    private final ExplainedFilter<VariationDiff<? extends DiffLinesLabel>> isGoodExample;
    private final PatchDiffRenderer exampleExport;

    /**
     * Creates a new ExampleFinder.
     * @param isGoodExample Function that decides whether a VariationDiff is an example candidate or not.
     *                      Should return {@link Optional#empty()} when the given tree is not a good example and thus, should not be considered.
     *                      Should return a VariationDiff when the given tree is a good example candidate and should be exported.
     *                      The returned VariationDiff might be the exact same VariationDiff or a subtree (e.g., to only export a certain subtree that is relevant).
     * @param renderer The renderer to use for rendering example candidates.
     */
    public ExampleFinder(final ExplainedFilter<VariationDiff<? extends DiffLinesLabel>> isGoodExample, VariationDiffRenderer renderer) {
        this.isGoodExample = isGoodExample;
        this.exampleExport = new PatchDiffRenderer(renderer, ExportOptions);
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) {
        final Repository currentRepo = analysis.getRepository();
        final VariationDiff<DiffLinesLabel> variationDiff = analysis.getCurrentVariationDiff();
        final CPPAnnotationParser annotationParser = analysis.getRepository().getParseOptions().variationDiffParseOptions().annotationParser();

        // We do not want a variationDiff for the entire file but only for the local change to have a small example.
        final VariationDiff<DiffLinesLabel> localTree;
        try {
            final String localDiff = getDiff(variationDiff);
            localTree = VariationDiff.fromDiff(localDiff, new VariationDiffParseOptions(annotationParser, true, true));
            // Not every local diff can be parsed to a VariationDiff because diffs are unaware of the underlying language (i.e., CPP).
            // We want only running examples whose diffs describe entire diff trees for easier understanding.
            if (isGoodExample.test(localTree)) {
                Assert.assertTrue(variationDiff.getSource() instanceof GitPatch);
                final GitPatch variationDiffSource = (GitPatch) variationDiff.getSource();
                localTree.setSource(variationDiffSource.shallowClone());
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

    private void exportExample(final VariationDiff<? extends DiffLinesLabel> example, final Path outputDir) {
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

    static String getDiff(final VariationDiff<?> tree) {
        final VariationDiffSource source = tree.getSource();
        Assert.assertTrue(source instanceof TextBasedDiff);
        return ((TextBasedDiff) source).getDiff();
    }
}

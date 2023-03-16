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
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.render.PatchDiffRenderer;
import org.variantsync.diffdetective.variation.diff.render.RenderOptions;
import org.variantsync.diffdetective.variation.diff.render.VariationDiffRenderer;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.MappingsDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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

    public static <T> List<List<T>> split(List<T> elements, Predicate<T> split) {
        final List<List<T>> result = new ArrayList<>();
        List<T> current = new ArrayList<>();

        for (final T t : elements) {
            if (split.test(t)) {
                if (!current.isEmpty()) {
                    result.add(current);
                    current = new ArrayList<>();
                }
            } else {
                current.add(t);
            }
        }

        if (!current.isEmpty()) {
            result.add(current);
        }

        return result;
    }

    private boolean checkIfExample(Analysis analysis, String localDiff) {
//        Logger.info(localDiff);

        final Repository currentRepo = analysis.getRepository();
        final VariationDiff<DiffLinesLabel> variationDiff = analysis.getCurrentVariationDiff();
        final CPPAnnotationParser annotationParser = analysis.getRepository().getParseOptions().variationDiffParseOptions().annotationParser();

        // We do not want a variationDiff for the entire file but only for the local change to have a small example.
        final VariationDiff<DiffLinesLabel> localTree;
        try {
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
                analysis,
                localDiff,
                localTree,
                analysis.getOutputDir()
        );

        return true;
    }

    @Override
    public void initializeResults(Analysis analysis) {
        Assert.assertEquals(analysis.getRepository().getParseOptions().diffStoragePolicy(), PatchDiffParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF);
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) {
        // We do not want a VariationDiff for the entire file but only for the local change to have a small example.
        // Analyze all patches individually
        final String localDiffs = getDiff(analysis.getCurrentVariationDiff());

        boolean exampleFound = false;
        for (List<String> diffLines : split(localDiffs.lines().toList(), s -> s.startsWith("@"))) {
            exampleFound |= checkIfExample(analysis, String.join(StringUtils.LINEBREAK, diffLines));
        }
        return exampleFound;
    }

    private void exportExample(final Analysis analysis, final String tdiff, final VariationDiff<DiffLinesLabel> vdiff, Path outputDir) {
        Assert.assertTrue(vdiff.getSource() instanceof GitPatch);
        final Repository repo = analysis.getRepository();
        final GitPatch patch = (GitPatch) vdiff.getSource();
        outputDir = outputDir.resolve(Path.of(repo.getRepositoryName() + "_" + patch.getCommitHash()));
        final String filename = patch.getFileName(Time.AFTER);

        Logger.info("Exporting example candidate: {}", patch);

        // export metadata
        String metadata = "";
        metadata += "Repository Name: " + repo.getRepositoryName() + StringUtils.LINEBREAK;
        metadata += "Repository URL: " + repo.getRemoteURI() + StringUtils.LINEBREAK;
        metadata += "Child commit: " + patch.getCommitHash() + StringUtils.LINEBREAK;
        metadata += "Parent commit: " + patch.getParentCommitHash() + StringUtils.LINEBREAK;
        metadata += "File: " + patch.getFileName(Time.AFTER) + StringUtils.LINEBREAK;
        String githubLink = repo.getRemoteURI().toString();
        if (githubLink.endsWith(".git")) {
            githubLink = githubLink.substring(0, githubLink.length() - ".git".length());
        }
        githubLink += "/commit/" + patch.getCommitHash();
        metadata += "Github: " + githubLink + StringUtils.LINEBREAK;
        IO.tryWrite(outputDir.resolve(filename + ".metadata.txt"), metadata);

        // export tdiff
        IO.tryWrite(outputDir.resolve(filename + ".diff"), tdiff);

        // export vdiff
        //exampleExport.render(example, patch, treeDir);
        try {
            Show.diff(vdiff).dontShowButRenderToTexture().saveAsPng(outputDir.resolve(patch.getFileName(Time.AFTER) + ".png").toFile());
        } catch (IOException e) {
            Logger.error("Could not render example");
        }
    }

    static String getDiff(final VariationDiff<?> tree) {
        final VariationDiffSource source = tree.getSource();
        Assert.assertTrue(source instanceof TextBasedDiff);
        return ((TextBasedDiff) source).getDiff();
    }
}

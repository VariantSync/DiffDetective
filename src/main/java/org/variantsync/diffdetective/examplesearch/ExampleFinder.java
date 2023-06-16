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
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
        final DiffTree diffTree = analysis.getCurrentDiffTree();
        final CPPAnnotationParser annotationParser = analysis.getRepository().getParseOptions().diffTreeParseOptions().annotationParser();

        // We do not want a difftree for the entire file but only for the local change to have a small example.
        final DiffTree localTree;
        try {
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
    public boolean analyzeDiffTree(Analysis analysis) {
        // We do not want a difftree for the entire file but only for the local change to have a small example.
        // Analyze all patches individually
        final String localDiffs = getDiff(analysis.getCurrentDiffTree());

        boolean exampleFound = false;
        for (List<String> diffLines : split(localDiffs.lines().toList(), s -> s.startsWith("@"))) {
            exampleFound |= checkIfExample(analysis, String.join(StringUtils.LINEBREAK, diffLines));
        }
        return exampleFound;
    }

    private void exportExample(final Analysis analysis, final String tdiff, final DiffTree vdiff, Path outputDir) {
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

    static String getDiff(final DiffTree tree) {
        final DiffTreeSource source = tree.getSource();
        Assert.assertTrue(source instanceof TextBasedDiff);
        return ((TextBasedDiff) source).getDiff();
    }
}

package org.variantsync.diffdetective.examplesearch;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.render.DiffTreeRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    /**
     * Modify this list to your requirements on a suitable example.
     */
    private static final ExplainedFilter<DiffTree> EXAMPLE_CRITERIONS = new ExplainedFilter<>(
            ExampleCriterions.MAX_LINE_COUNT(ExampleCriterions.DefaultMaxDiffLineCount),
            ExampleCriterions.DOES_NOT_CONTAIN_ANNOTATED_MACROS,
            ExampleCriterions.HAS_EDITED_ARTIFACTS,
            ExampleCriterions.HAS_ADDITIONS,
            ExampleCriterions.HAS_DELETIONS,
            ExampleCriterions.HAS_NESTING,
            ExampleCriterions.HAS_ELSE,
            ExampleCriterions.MIN_PARALLEL_EDITS(3),
            ExampleCriterions.MIN_CHANGES_TO_PCS(1),
            ExampleCriterions.MIN_NODES_OF_TYPE(NodeType.IF, 3),
            ExampleCriterions.MIN_FEATURES(2),
            //ExampleCriterions.MIN_ANNOTATIONS(4), // root + if + else + something else
            DiffTreeFilter.hasAtLeastOneEditToVariability()
    );

    public static Analysis findExamplesIn(Repository repo, Path repoOutputDir) {
        return new Analysis(
                "Find Running Examples in " + repo.getRepositoryName(),
                List.of(new ExampleFinder(
                        EXAMPLE_CRITERIONS,
                        DiffTreeRenderer.WithinDiffDetective()
                )),
                repo,
                repoOutputDir
        );
    }
    public static void main(String[] args) throws IOException {
        final AnalysisRunner.Options defaultOptions = AnalysisRunner.Options.DEFAULT(args);

        final AnalysisRunner.Options myOptions = new AnalysisRunner.Options(
                defaultOptions.repositoriesDirectory(),
                ExampleCriterions.DefaultExamplesDirectory,
                Path.of("docs", "datasets", "reposToSearchForExamples.md"),
                repo -> repo.getParseOptions().withDiffStoragePolicy(
                        PatchDiffParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF
                ),
                defaultOptions.getFilterForRepo(),
                false,
                false
        );

        AnalysisRunner.run(myOptions, (repo, repoOutputDir) ->
                Analysis.forEachCommit(() -> findExamplesIn(repo, repoOutputDir))
        );
    }
}

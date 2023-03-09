package org.variantsync.diffdetective.examplesearch;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.ParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.render.DiffTreeRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    private static final ExplainedFilter<DiffTree> EXAMPLE_CRITERIONS = new ExplainedFilter<>(
            ExampleCriterions.MAX_LINE_COUNT(ExampleCriterions.DefaultMaxDiffLineCount),
            ExampleCriterions.HAS_EDITED_ARTIFACTS,
            ExampleCriterions.DOES_NOT_CONTAIN_ANNOTATED_MACROS,
            ExampleCriterions.MIN_ANNOTATIONS(2),
            ExampleCriterions.HAS_ELSE
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
                defaultOptions.datasetsFile(),
                ParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF,
                false,
                false
        );

        AnalysisRunner.run(myOptions, (repo, repoOutputDir) ->
                Analysis.forEachCommit(() -> findExamplesIn(repo, repoOutputDir))
        );
    }
}

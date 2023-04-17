package org.variantsync.diffdetective.experiments.views;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static Analysis AnalysisFactory(Repository repo, Path repoOutputDir) {
        return new Analysis(
                "Views Analysis",
                List.of(
                        new PreprocessingAnalysis(new CutNonEditedSubtrees(true)),
                        new FilterAnalysis( // filters unwanted trees
                                DiffTreeFilter.notEmpty()
                        ),
                        new ViewAnalysis(),
                        new StatisticsAnalysis()
                ),
                repo,
                repoOutputDir
        );
    }

    public static void main(String[] args) throws IOException {
        final AnalysisRunner.Options defaultOptions = AnalysisRunner.Options.DEFAULT(args);
        final AnalysisRunner.Options analysisOptions = new AnalysisRunner.Options(
                defaultOptions.repositoriesDirectory(),
                Paths.get("results", "views", "current"),
                defaultOptions.datasetsFile(),
                repo -> new PatchDiffParseOptions(
                        PatchDiffParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER,
                        new DiffTreeParseOptions(
                                true,
                                false
                        )
                ),
                defaultOptions.getFilterForRepo(),
                true,
                false
        );

        AnalysisRunner.run(analysisOptions, (repository, path) ->
                Analysis.forEachCommit(() -> AnalysisFactory(repository, path))
        );
    }
}
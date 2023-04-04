package org.variantsync.diffdetective.experiments.views;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static Analysis AnalysisFactory(Repository repo, Path repoOutputDir) {
        return new Analysis(
                "Views Analysis",
                List.of(
//                      new PreprocessingAnalysis(new CutNonEditedSubtrees()),
                        new FilterAnalysis(DiffTreeFilter.notEmpty()), // filters unwanted trees
//                        new ViewAnalysis(),
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
                defaultOptions.getParseOptionsForRepo(),
                defaultOptions.getFilterForRepo(),
                true,
                false
        );

        AnalysisRunner.run(analysisOptions, (repository, path) ->
                Analysis.forEachCommit(() -> AnalysisFactory(repository, path), 100, 1)
        );
    }
}
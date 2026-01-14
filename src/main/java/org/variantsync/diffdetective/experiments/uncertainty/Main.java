package org.variantsync.diffdetective.experiments.uncertainty;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.experiments.views.ViewAnalysis;
import org.variantsync.diffdetective.variation.diff.filter.VariationDiffFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.tree.view.relevance.Search;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for running the feasibility study (Section 6) of our SPLC'23 paper
 * Views on Edits to Variational Software.
 */
public class Main {
    /*
     * There is a bug in DiffView::naive when ignoreEmptyLines is set to true under
     * some very rare circumstances that I do not know so far. The bug occurred for
     * repo: emacs
     * commit: 2254b6c09cff8f3a83684fd159289d0e305b0e7d
     * patch: "src/alloc.c"
     * view_naive
     * with the following relevance.
     * What is weird that the parsing only failed when running the analyses but not when extracting the diff to a unit
     * test and parsing it there.
     * Maybe it has something to do with linebreak or whitespace characters?
     */
    private static final Search bugRelevance = new Search("  /* Check both of the above conditions, for symbols.  */");
    public static VariationDiffParseOptions VARIATION_DIFF_PARSE_OPTIONS =
            new VariationDiffParseOptions(
                    true,
                    false
            );

    /**
     * Creates the analysis to perform on the given repository to run our uncertainty search.
     * @param repo The repository to run the uncertainty search on.
     * @param repoOutputDir The directory to which output should be written.
     * @return The analysis to run.
     */
    private static Analysis AnalysisFactory(Repository repo, Path repoOutputDir) {
        return new Analysis(
                "Uncertainty Analysis",
                new ArrayList<>(List.of(
                        //*
                        new PreprocessingAnalysis(
                            new CutNonEditedSubtrees<>()
                        ),
                        //**/
                        new FilterAnalysis( // filters unwanted trees
                                VariationDiffFilter.notEmpty()
                        ),
                        new DebugAnalysis()//,
//                        new StatisticsAnalysis()
                )),
                repo,
                repoOutputDir
        );
    }

    /**
     * Main method for running the uncertainty search.
     * @param args see {@link AnalysisRunner.Options#DEFAULT(String[])}
     * @throws IOException When an IO operation within the feasibility study fails.
     */
    public static void main(String[] args) throws IOException {
        final AnalysisRunner.Options defaultOptions = AnalysisRunner.Options.DEFAULT(args);
        final AnalysisRunner.Options analysisOptions = new AnalysisRunner.Options(
                Paths.get("..", "DiffDetectiveReplicationDatasets"),
                Paths.get("results", "uncertainty", "current"),
                defaultOptions.datasetsFile(),
                repo -> new PatchDiffParseOptions(
                        PatchDiffParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER,
                        VARIATION_DIFF_PARSE_OPTIONS
                ),
                defaultOptions.getFilterForRepo(),
                true,
                false
        );

        AnalysisRunner.run(analysisOptions, (repository, path) -> {
            Analysis.forEachCommit(() -> AnalysisFactory(repository, path)
//                    , 1000, 1
            );
        });
    }
}

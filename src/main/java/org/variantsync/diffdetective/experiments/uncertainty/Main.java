package org.variantsync.diffdetective.experiments.uncertainty;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.variation.diff.filter.VariationDiffFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.tree.view.relevance.Search;

import java.io.IOException;
import java.nio.file.Files;
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
    private static Analysis AnalysisFactory(Repository repo, Path repoOutputDir, DebugAnalysis debugAnalysis) {
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
                debugAnalysis
                //new DebugAnalysis()//,
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

//        AnalysisRunner.run(analysisOptions, (repository, path) -> {
//        DebugAnalysis debugAnalysis = new DebugAnalysis();
//            Analysis.forEachCommit(() -> AnalysisFactory(repository, path, debugAnalysis)
////                    , 1000, 1
//            );
//        });

        Path outputDir = Paths.get("results", "uncertainty");
        Files.createDirectories(outputDir);

        Path outputFile = outputDir.resolve("repos_summary.txt");

        try (var writer = Files.newBufferedWriter(outputFile)) {
            AnalysisRunner.run(analysisOptions, (repository, path) -> {
                DebugAnalysis debugAnalysis = new DebugAnalysis();
                Analysis.forEachCommit(() -> AnalysisFactory(repository, path, debugAnalysis)
                    , 1000, 1
                );
                try {
                    writer.write("Repository: " + repository.getRemoteURI());
                    writer.newLine();
                    writer.write("Total number of commits: " + debugAnalysis.getTotalCommitNumber());
                    writer.newLine();
                    writer.write("Number of commits with search-string but without classification filter: " + debugAnalysis.getCommitsWithSearchButWithoutClassification());
                    writer.newLine();
                    writer.write("Number of commits with classification but without search-string filter: " + debugAnalysis.getCommitsWithClassificationButWithoutSearch());
                    writer.newLine();
                    writer.write("Number of commits with classification and with search-string filter: " + debugAnalysis.getFinalCommitNumber());
                    writer.newLine();
                    writer.write("----------------------------------------------");
                    writer.newLine();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println(e.getMessage());
            //e.printStackTrace();
        }


    }
}

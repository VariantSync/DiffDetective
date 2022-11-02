package org.variantsync.diffdetective.mining;

import org.apache.commons.io.FileUtils;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTaskFactory;
import org.variantsync.diffdetective.analysis.HistoryAnalysis;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.analysis.strategies.AnalyzeAllThenExport;
import org.variantsync.diffdetective.datasets.*;
import org.variantsync.diffdetective.datasets.predefined.StanciulescuMarlin;
import org.variantsync.diffdetective.diff.difftree.filter.DiffTreeFilter;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.transform.CollapseNestedNonEditedAnnotations;
import org.variantsync.diffdetective.diff.difftree.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.difftree.transform.Starfold;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.mining.formats.DirectedEdgeLabelFormat;
import org.variantsync.diffdetective.mining.formats.MiningNodeFormat;
import org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DiffTreeMiner {
    public static final Path DATASET_FILE = DefaultDatasets.EMACS;
    public static final boolean SEARCH_FOR_GOOD_RUNNING_EXAMPLES = false;
    public static final boolean UPDATE_REPOS_BEFORE_MINING = false;
//    public static final boolean PRINT_LATEX_TABLE = true;
//    public static final int PRINT_LARGEST_SUBJECTS = 3;
    public static final boolean DEBUG_TEST = false;

    public static List<DiffTreeTransformer> Postprocessing(final Repository repository) {
        final List<DiffTreeTransformer> processing = new ArrayList<>();
        processing.add(new CutNonEditedSubtrees());
        if (SEARCH_FOR_GOOD_RUNNING_EXAMPLES) {
            processing.add(new RunningExampleFinder(repository == null ? CPPAnnotationParser.Default : repository.getParseOptions().annotationParser()).
                    The_Diff_Itself_Is_A_Valid_DiffTree_And(
                            RunningExampleFinder.DefaultExampleConditions,
                            RunningExampleFinder.DefaultExamplesDirectory.resolve(repository == null ? "unknown" : repository.getRepositoryName())
                    ));
        }
        processing.add(new CollapseNestedNonEditedAnnotations());
        processing.add(Starfold.IgnoreNodeOrder());
        return processing;
    }

    public static MiningNodeFormat NodeFormat() {
        return
//                new DebugMiningDiffNodeFormat();
                new ReleaseMiningDiffNodeFormat();
    }

    public static EdgeLabelFormat EdgeFormat() {
        return EdgeFormat(NodeFormat());
    }

    private static EdgeLabelFormat EdgeFormat(final MiningNodeFormat nodeFormat) {
        final EdgeLabelFormat.Direction direction = EdgeLabelFormat.Direction.ParentToChild;
        return
//                new DefaultEdgeLabelFormat(direction);
                new DirectedEdgeLabelFormat(nodeFormat, false, direction);
    }

    public static LineGraphExportOptions MiningExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new LineGraphExportOptions(
                  GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
                , nodeFormat
                , EdgeFormat(nodeFormat)
                , LineGraphExportOptions.LogError()
                .andThen(LineGraphExportOptions.RenderError())
                .andThen(LineGraphExportOptions.SysExitOnError())
        );
    }

    public static AnalysisStrategy MiningStrategy() {
        return new AnalyzeAllThenExport();
//                new CompositeDiffTreeMiningStrategy(
//                        new MineAndExportIncrementally(1000),
//                        new MiningMonitor(10)
//                );
    }

    public static CommitHistoryAnalysisTaskFactory Mine() {
        return (repo, differ, outputPath, commits) -> new MiningTask(new CommitHistoryAnalysisTask.Options(
                repo,
                differ,
                outputPath,
                new ExplainedFilter<>(
                        DiffTreeFilter.notEmpty(),
                        DiffTreeFilter.moreThanOneArtifactNode(),
                        /// We want to exclude patches that do not edit variability.
                        /// In particular, we noticed that most edits just insert or delete artifacts (or replace it).
                        /// This is reasonable and was also observed in previous studies: Edits to artifacts are more frequent than edits to variability.
                        /// Yet, such edits cannot reveal compositions of more complex edits to variability.
                        /// We thus filter them.
                        DiffTreeFilter.hasAtLeastOneEditToVariability()
                ),
                Postprocessing(repo),
                MiningStrategy(),
                commits
        ), MiningExportOptions(repo));
    }

    public static void main(String[] args) throws IOException {
//        setupLogger(Level.INFO);
//        setupLogger(Level.DEBUG);

        final ParseOptions.DiffStoragePolicy diffStoragePolicy = ParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER;

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path outputDir = Paths.get("results", "difftrees");

        final List<Repository> repos;

        if (DEBUG_TEST) {
            final Path variantEvolutionDatasetsDir = Paths.get("..", "variantevolution_datasets");

            repos = List.of(
//                Godot.cloneFromGithubTo(inputDir),
                    StanciulescuMarlin.fromZipInDiffDetectiveAt(Path.of("."))
//                Vim.cloneFromGithubTo(inputDir),
//                LinuxKernel.cloneFromGithubTo(variantEvolutionDatasetsDir)
            );
        } else {
            final List<DatasetDescription> datasets = DefaultDatasets.loadDatasets(DATASET_FILE);

//            if (PRINT_LATEX_TABLE) {
//                Validation.printLaTeXTableFor(datasets);
//            }

            final DatasetFactory miningDatasetFactory = new DatasetFactory(inputDir);
            repos = miningDatasetFactory.createAll(datasets, true, UPDATE_REPOS_BEFORE_MINING);
        }

        Logger.info("Mining the following repositories:");
        for (final Repository repo : repos) {
            repo.setParseOptions(repo.getParseOptions().withDiffStoragePolicy(diffStoragePolicy));
            Logger.info("  - {} from {}", repo.getRepositoryName(), repo.getRemoteURI());
        }

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        final Consumer<Path> repoPostProcessing;
        if (SEARCH_FOR_GOOD_RUNNING_EXAMPLES) {
            repoPostProcessing = repoOutputDir -> {
                new ExplainedFilterSummary(RunningExampleFinder.DefaultExampleConditions).exportTo(repoOutputDir.resolve("runningExampleFilterReasons.txt"));
                RunningExampleFinder.DefaultExampleConditions.resetExplanations();
            };
        } else {
            repoPostProcessing = p -> {};
        }

        final HistoryAnalysis analysis = new HistoryAnalysis(
                repos,
                outputDir,
                HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT,
                Mine(),
                repoPostProcessing);
        analysis.runAsync();
        Logger.info("Done");

        final String logFile = "log.txt";
        FileUtils.copyFile(Path.of(logFile).toFile(), outputDir.resolve(logFile).toFile());
    }
}

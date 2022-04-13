package mining;

import analysis.CommitHistoryAnalysisTask;
import analysis.CommitHistoryAnalysisTaskFactory;
import analysis.HistoryAnalysis;
import analysis.strategies.AnalysisStrategy;
import analysis.strategies.AnalyzeAllThenExport;
import datasets.*;
import datasets.predefined.StanciulescuMarlin;
import diff.difftree.filter.DiffTreeFilter;
import diff.difftree.filter.ExplainedFilter;
import diff.difftree.parse.DiffNodeParser;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.difftree.transform.CollapseNestedNonEditedMacros;
import diff.difftree.transform.CutNonEditedSubtrees;
import diff.difftree.transform.DiffTreeTransformer;
import metadata.ExplainedFilterSummary;
import mining.formats.DirectedEdgeLabelFormat;
import mining.formats.MiningNodeFormat;
import mining.formats.ReleaseMiningDiffNodeFormat;
import org.apache.commons.io.FileUtils;
import org.tinylog.Logger;
import validation.Validation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DiffTreeMiner {
    public final static Path DATASETS_FILE = Path.of("docs", "datasets.md");
//    public static final int COMMITS_TO_PROCESS_PER_THREAD = 10000;

    public static final boolean SEARCH_FOR_GOOD_RUNNING_EXAMPLES = false;
    public static final boolean UPDATE_REPOS_BEFORE_MINING = false;
    public static final boolean PRINT_LATEX_TABLE = true;
    public static final int PRINT_LARGEST_SUBJECTS = 3;
    public static final boolean DEBUG_TEST = false;

    public static List<DiffTreeTransformer> Postprocessing() {
        return Postprocessing(null);
    }

    public static List<DiffTreeTransformer> Postprocessing(final Repository repository) {
        final List<DiffTreeTransformer> processing = new ArrayList<>();
        processing.add(new CutNonEditedSubtrees());
        if (SEARCH_FOR_GOOD_RUNNING_EXAMPLES) {
            processing.add(new RunningExampleFinder(repository == null ? DiffNodeParser.Default : repository.getParseOptions().annotationParser()).
                    The_Diff_Itself_Is_A_Valid_DiffTree_And(
                            RunningExampleFinder.DefaultExampleConditions,
                            RunningExampleFinder.DefaultExamplesDirectory.resolve(repository == null ? "unknown" : repository.getRepositoryName())
                    ));
        }
        processing.add(new CollapseNestedNonEditedMacros());
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

    public static DiffTreeLineGraphExportOptions MiningExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new DiffTreeLineGraphExportOptions(
                  GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
                , nodeFormat
                , EdgeFormat(nodeFormat)
                , new ExplainedFilter<>(
                        DiffTreeFilter.notEmpty(),
                        DiffTreeFilter.moreThanOneCodeNode(),
                        /// We want to exclude patches that do not edit variability.
                        /// In particular, we noticed that most edits just insert or delete code (or replace it).
                        /// This is reasonable and was also observed in previous studies: Edits to code are more frequent than edits to variability.
                        /// Yet, such edits cannot reveal compositions of more complex edits to variability.
                        /// We thus filter them.
                        DiffTreeFilter.hasAtLeastOneEditToVariability()
                )
                , Postprocessing(repository)
                , DiffTreeLineGraphExportOptions.LogError()
                .andThen(DiffTreeLineGraphExportOptions.RenderError())
                .andThen(DiffTreeLineGraphExportOptions.SysExitOnError())
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
                MiningExportOptions(repo),
                MiningStrategy(),
                commits
        ));
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
            final List<DatasetDescription> datasets = DefaultDatasets.loadDefaultDatasets();

            if (PRINT_LATEX_TABLE) {
                Validation.printLaTeXTableFor(datasets);
            }

            final DatasetFactory miningDatasetFactory = new DatasetFactory(inputDir);
            repos = miningDatasetFactory.createAll(datasets, true, UPDATE_REPOS_BEFORE_MINING);
        }

        Logger.info("Mining the following repositories:");
        for (final Repository repo : repos) {
            repo.setParseOptions(repo.getParseOptions().withDiffStoragePolicy(diffStoragePolicy));
            Logger.info("  - " + repo.getRepositoryName() + " from " + repo.getRemoteURI());
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

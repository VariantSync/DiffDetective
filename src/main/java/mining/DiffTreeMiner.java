package mining;

import datasets.ParseOptions;
import datasets.Repository;
import datasets.predefined.StanciulescuMarlin;
import de.variantsync.functjonal.iteration.ClusteredIterator;
import de.variantsync.functjonal.iteration.MappedIterator;
import diff.GitDiffer;
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
import metadata.Metadata;
import mining.dataset.MiningDataset;
import mining.dataset.MiningDatasetFactory;
import mining.formats.DirectedEdgeLabelFormat;
import mining.formats.MiningNodeFormat;
import mining.formats.ReleaseMiningDiffNodeFormat;
import mining.monitoring.TaskCompletionMonitor;
import mining.strategies.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import parallel.ScheduledTasksIterator;
import util.Assert;
import util.Clock;
import util.Diagnostics;
import util.InvocationCounter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DiffTreeMiner {
    private final static Diagnostics DIAGNOSTICS = new Diagnostics();
    public final static Path DATASETS_FILE = Path.of("docs", "datasets.md");
//    public static final int COMMITS_TO_PROCESS_PER_THREAD = 10000;
    public static final int COMMITS_TO_PROCESS_PER_THREAD = 1000;
    public static final int EXPECTED_NUMBER_OF_COMMITS_IN_LINUX = 495284;
    public static final String TOTAL_RESULTS_FILE_NAME = "totalresult" + DiffTreeMiningResult.EXTENSION;

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

    public static DiffTreeLineGraphExportOptions ValidationExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
                , nodeFormat
                , EdgeFormat(nodeFormat)
                , new ExplainedFilter<>(DiffTreeFilter.notEmpty())
                , List.of(new CutNonEditedSubtrees())
                , DiffTreeLineGraphExportOptions.LogError()
                .andThen(DiffTreeLineGraphExportOptions.RenderError())
                .andThen(DiffTreeLineGraphExportOptions.SysExitOnError())
        );
    }

    public static DiffTreeMiningStrategy MiningStrategy() {
        return new MineAllThenExport();
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

    public static CommitHistoryAnalysisTaskFactory Validate() {
        return (repo, differ, outputPath, commits) -> new PatternValidation(new CommitHistoryAnalysisTask.Options(
                repo,
                differ,
                outputPath,
                ValidationExportOptions(repo),
                new NullStrategy(),
                commits
        ));
    }

    @Deprecated
    public static void mine(
            final Repository repo,
            final Path outputDir,
            final DiffTreeLineGraphExportOptions exportOptions,
            final DiffTreeMiningStrategy strategy)
    {
        DiffTreeMiningResult totalResult;
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        Logger.info(">>> Scheduling synchronous mining");
        clock.start();
        List<RevCommit> commitsToProcess = differ.yieldRevCommits().toList();
        final CommitHistoryAnalysisTask task = new MiningTask(new CommitHistoryAnalysisTask.Options(
                repo,
                differ,
                outputDir.resolve(repo.getRepositoryName() + ".lg"),
                exportOptions,
                strategy,
                commitsToProcess
                ));
        Logger.info("Scheduled " + commitsToProcess.size() + " commits.");
        commitsToProcess = null; // free reference to enable garbage collection
        Logger.info("<<< done after " + clock.printPassedSeconds());

        Logger.info(">>> Run mining");
        clock.start();
        try {
            totalResult = task.call();
        } catch (Exception e) {
            Logger.error(e);
            Logger.info("<<< aborted after " + clock.printPassedSeconds());
            return;
        }
        Logger.info("<<< done after " + clock.printPassedSeconds());

        exportMetadata(outputDir, totalResult);
    }

    public static void mineAsync(
            final Repository repo,
            final Path outputDir,
            final CommitHistoryAnalysisTaskFactory taskFactory)
    {
        final DiffTreeMiningResult totalResult = new DiffTreeMiningResult(repo.getRepositoryName());
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        final int nThreads = DIAGNOSTICS.getNumberOfAvailableProcessors();
        Logger.info(">>> Scheduling asynchronous mining on " + nThreads + " threads.");
        clock.start();
        final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
        final Iterator<CommitHistoryAnalysisTask> tasks = new MappedIterator<>(
                /// 1.) Retrieve COMMITS_TO_PROCESS_PER_THREAD commits from the differ and cluster them into one list.
                new ClusteredIterator<>(differ.yieldRevCommitsAfter(numberOfTotalCommits), COMMITS_TO_PROCESS_PER_THREAD),
                /// 2.) Create a MiningTask for the list of commits. This task will then be processed by one
                ///     particular thread.
                commitList -> taskFactory.create(
                    repo,
                    differ,
                    outputDir.resolve(commitList.get(0).getId().getName() + ".lg"),
                    commitList)
        );
        Logger.info("<<< done in " + clock.printPassedSeconds());

        final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
        Logger.info(">>> Run mining");
        clock.start();
        commitSpeedMonitor.start();
        try (final ScheduledTasksIterator<DiffTreeMiningResult> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                final DiffTreeMiningResult threadsResult = threads.next();
                totalResult.append(threadsResult);
                commitSpeedMonitor.addFinishedTasks(threadsResult.exportedCommits);
            }
        } catch (Exception e) {
            Logger.error("Failed to run all mining task!");
            Logger.error(e);
        }

        final double runtime = clock.getPassedSeconds();
        Logger.info("<<< done in " + Clock.printPassedSeconds(runtime));

        totalResult.totalCommits = numberOfTotalCommits.invocationCount().get();

        exportMetadata(outputDir, totalResult);
    }

    public static <T> void exportMetadata(final Path outputDir, final Metadata<T> totalResult) {
        exportMetadataToFile(outputDir.resolve(TOTAL_RESULTS_FILE_NAME), totalResult);
    }

    public static <T> void exportMetadataToFile(final Path outputFile, final Metadata<T> totalResult) {
        final String prettyMetadata = totalResult.exportTo(outputFile);
        Logger.info("Metadata:\n" + prettyMetadata);
    }

    public static void main(String[] args) {
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
            final List<MiningDataset> datasets;
            try {
                datasets = MiningDataset.fromMarkdown(DATASETS_FILE);
            } catch (IOException e) {
                Logger.error("Failed to load at least one dataset from " + DATASETS_FILE + " because:", e);
                Logger.error("Aborting execution!");
                return;
            }

            if (PRINT_LATEX_TABLE) {
                Logger.info("Its dangerous outside. Take this!");
                System.out.println(MiningDataset.asLaTeXTable(datasets));

                Logger.info("The " + PRINT_LARGEST_SUBJECTS + " largest systems are:");
                final Comparator<MiningDataset> larger = (a, b) -> {
                    final int ai = Integer.parseInt(a.commits().replaceAll(",", ""));
                    final int bi = Integer.parseInt(b.commits().replaceAll(",", ""));
                    return -Integer.compare(ai, bi);
                };
                final List<MiningDataset> largestDatasets = datasets.stream()
                        .sorted(larger)
                        .limit(PRINT_LARGEST_SUBJECTS)
                        .collect(Collectors.toList());
                datasets.stream()
                        .filter(m -> m.name().equalsIgnoreCase("Marlin")
                                || m.name().equalsIgnoreCase("libssh")
                                || m.name().equalsIgnoreCase("Busybox")
                                || m.name().equalsIgnoreCase("Godot"))
                        .forEach(largestDatasets::add);
                largestDatasets.sort(larger);
                System.out.println(MiningDataset.asLaTeXTable(largestDatasets));
            }

            final MiningDatasetFactory miningDatasetFactory = new MiningDatasetFactory(inputDir);
            repos = datasets.stream().map(miningDatasetFactory::create).collect(Collectors.toList());
        }

        Logger.info("Mining the following repositories:");
        for (final Repository repo : repos) {
            repo.setParseOptions(repo.getParseOptions().withDiffStoragePolicy(diffStoragePolicy));
            Logger.info("  - " + repo.getRepositoryName() + " from " + repo.getRemoteURI());
        }

        Logger.info("Preloading repositories:");
        for (final Repository repo : repos) {
            repo.getGitRepo().run();
        }

        if (UPDATE_REPOS_BEFORE_MINING) {
            Logger.info("Updating repositories:");
            for (final Repository repo : repos) {
                try {
                    Assert.assertTrue(repo.getGitRepo().run().pull().call().isSuccessful());
                } catch (GitAPIException e) {
                    Logger.error("Failed to pull repository \"" + repo.getRepositoryName() + "\"!", e);
                }
            }
        }

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        for (final Repository repo : repos) {
            Logger.info(" === Begin Processing " + repo.getRepositoryName() + " ===");
            final Clock clock = new Clock();
            clock.start();

            final Path repoOutputDir = outputDir.resolve(repo.getRepositoryName());
            /// Don't repeat work we already did:
            if (!Files.exists(repoOutputDir.resolve(TOTAL_RESULTS_FILE_NAME))) {
                mineAsync(repo, repoOutputDir, Validate());
//                mine(repo, repoOutputDir, ExportOptions(repo), MiningStrategy());

                if (SEARCH_FOR_GOOD_RUNNING_EXAMPLES) {
                    new ExplainedFilterSummary(RunningExampleFinder.DefaultExampleConditions).exportTo(repoOutputDir.resolve("runningExampleFilterReasons.txt"));
                    RunningExampleFinder.DefaultExampleConditions.resetExplanations();
                }
            } else {
                Logger.info("  Skipping repository " + repo.getRepositoryName() + " because it has already been processed.");
            }

            Logger.info(" === End Processing " + repo.getRepositoryName() + " after " + clock.printPassedSeconds() + " ===");
        }

        Logger.info("Done");
    }
}

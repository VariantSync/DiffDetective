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
import diff.difftree.transform.FeatureExpressionFilter;
import metadata.ExplainedFilterSummary;
import metadata.Metadata;
import mining.dataset.MiningDataset;
import mining.dataset.MiningDatasetFactory;
import mining.formats.DirectedEdgeLabelFormat;
import mining.formats.MiningNodeFormat;
import mining.formats.ReleaseMiningDiffNodeFormat;
import mining.monitoring.TaskCompletionMonitor;
import mining.strategies.DiffTreeMiningStrategy;
import mining.strategies.MineAllThenExport;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import parallel.ScheduledTasksIterator;
import util.Assert;
import util.Clock;
import util.Diagnostics;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DiffTreeMiner {
    private final static Diagnostics DIAGNOSTICS = new Diagnostics();
//    public static final int COMMITS_TO_PROCESS_PER_THREAD = 10000;
    public static final int COMMITS_TO_PROCESS_PER_THREAD = 1000;
    public static final int EXPECTED_NUMBER_OF_COMMITS_IN_LINUX = 495284;

    public static final boolean SEARCH_FOR_GOOD_RUNNING_EXAMPLES = false;
    public static final boolean UPDATE_REPOS_BEFORE_MINING = false;
    public static final boolean PRINT_LATEX_TABLE = false;
    public static final boolean DEBUG_TEST = false;

    public static List<DiffTreeTransformer> Postprocessing() {
        return Postprocessing(null);
    }

    public static List<DiffTreeTransformer> Postprocessing(final Repository repository) {
        final List<DiffTreeTransformer> processing = new ArrayList<>();
        processing.add(new CutNonEditedSubtrees());
        if (repository != null && repository.hasFeatureAnnotationFilter()) {
            processing.add(new FeatureExpressionFilter(repository.getFeatureAnnotationFilter()));
        }
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

    public static DiffTreeLineGraphExportOptions ExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new DiffTreeLineGraphExportOptions(
                  GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
                , nodeFormat
                , EdgeFormat(nodeFormat)
                , new ExplainedFilter<>(
                        DiffTreeFilter.notEmpty(),
                        DiffTreeFilter.moreThanTwoCodeNodes(),
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

    public static DiffTreeMiningStrategy MiningStrategy() {
        return new MineAllThenExport();
//                new CompositeDiffTreeMiningStrategy(
//                        new MineAndExportIncrementally(1000),
//                        new MiningMonitor(10)
//                );
    }

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
        final MiningTask task = new MiningTask(
                repo,
                differ,
                outputDir.resolve(repo.getRepositoryName() + ".lg"),
                exportOptions,
                strategy,
                commitsToProcess
                );
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
            final Function<Repository, DiffTreeLineGraphExportOptions> exportOptions,
            final Supplier<DiffTreeMiningStrategy> strategyFactory)
    {
        final DiffTreeMiningResult totalResult = new DiffTreeMiningResult();
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        final int nThreads = DIAGNOSTICS.getNumberOfAvailableProcessors();
        Logger.info(">>> Scheduling asynchronous mining on " + nThreads + " threads.");
        clock.start();
        final Iterator<MiningTask> tasks = new MappedIterator<>(
                /// 1.) Retrieve COMMITS_TO_PROCESS_PER_THREAD commits from the differ and cluster them into one list.
                new ClusteredIterator<>(differ.yieldRevCommits(), COMMITS_TO_PROCESS_PER_THREAD),
                /// 2.) Create a MiningTask for the list of commits. This task will then be processed by one
                ///     particular thread.
                commitList -> new MiningTask(
                    repo,
                    differ,
                    outputDir.resolve(commitList.get(0).getId().getName() + ".lg"),
                    exportOptions.apply(repo),
                    strategyFactory.get(),
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
        final String runtime = clock.printPassedSeconds();
        Logger.info("<<< done in " + runtime);

        totalResult.putCustomInfo("runtime in seconds", runtime);

        exportMetadata(outputDir, totalResult);
    }

    public static <T> void exportMetadata(final Path outputDir, final Metadata<T> totalResult) {
        final String prettyMetadata = totalResult.exportTo(outputDir.resolve("totalresult" + DiffTreeMiningResult.EXTENSION));
        Logger.info("Metadata:\n" + prettyMetadata);
    }


    public static void main(String[] args) {
//        setupLogger(Level.INFO);
//        setupLogger(Level.DEBUG);

        final ParseOptions.DiffStoragePolicy diffStoragePolicy = ParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF;

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
            final Path datasetsFile = Path.of("docs", "datasets.md");

            final List<MiningDataset> datasets;
            try {
                datasets = MiningDataset.fromMarkdown(datasetsFile);
            } catch (IOException e) {
                Logger.error("Failed to load at least one dataset from " + datasetsFile + " because:", e);
                Logger.error("Aborting execution!");
                return;
            }

            if (PRINT_LATEX_TABLE) {
                Logger.info("Its dangerous outside. Take this!");
                System.out.println(MiningDataset.asLaTeXTable(datasets));
            }

            final MiningDatasetFactory miningDatasetFactory = new MiningDatasetFactory(inputDir);
            repos = datasets.stream().map(miningDatasetFactory::create).collect(Collectors.toList());
        }

        Logger.info("Mining the following repositories:");
        for (final Repository repo : repos) {
            repo.setParseOptions(repo.getParseOptions().withDiffStoragePolicy(diffStoragePolicy));
            Logger.info("  - " + repo.getRepositoryName() + " from " + repo.getRemoteURI());
        }

        if (2 == 1 + 1) {
            return;
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
            mineAsync(repo, repoOutputDir, DiffTreeMiner::ExportOptions, DiffTreeMiner::MiningStrategy);
//            mine(repo, repoOutputDir, ExportOptions(repo), MiningStrategy());

            if (SEARCH_FOR_GOOD_RUNNING_EXAMPLES) {
                new ExplainedFilterSummary(RunningExampleFinder.DefaultExampleConditions).exportTo(repoOutputDir.resolve("runningExampleFilterReasons.txt"));
                RunningExampleFinder.DefaultExampleConditions.resetExplanations();
            }

            Logger.info(" === End Processing " + repo.getRepositoryName() + " after " + clock.printPassedSeconds() + " ===");
        }

        Logger.info("Done");
    }
}

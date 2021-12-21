package main.mining;

import datasets.DebugOptions;
import datasets.DefaultRepositories;
import datasets.Repository;
import diff.GitDiffer;
import diff.difftree.filter.DiffTreeFilter;
import diff.difftree.filter.TaggedPredicate;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.difftree.transform.CollapseNestedNonEditedMacros;
import diff.difftree.transform.CutNonEditedSubtrees;
import diff.difftree.transform.DiffTreeTransformer;
import main.Main;
import main.mining.formats.ReleaseMiningDiffNodeFormat;
import main.mining.monitoring.TaskCompletionMonitor;
import main.mining.strategies.DiffTreeMiningStrategy;
import main.mining.strategies.MineAllThenExport;
import org.eclipse.jgit.revwalk.RevCommit;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import parallel.ScheduledTasksIterator;
import util.Clock;
import util.ClusteredIterator;
import util.Diagnostics;
import util.MappedIterator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class DiffTreeMiner {
    private final static Diagnostics DIAGNOSTICS = new Diagnostics();
//    public static final int COMMITS_TO_PROCESS_PER_THREAD = 10000;
    public static final int COMMITS_TO_PROCESS_PER_THREAD = 1000;
    public static final int EXPECTED_NUMBER_OF_COMMITS_IN_LINUX = 495284;

    public static List<DiffTreeTransformer> Postprocessing() {
        return List.of(
//                new NaiveMovedCodeDetection(), // do this first as it might introduce non-edited subtrees
                new CutNonEditedSubtrees(),
//                RunningExampleFinder.Default,
                new CollapseNestedNonEditedMacros()
        );
    }

    public static DiffTreeLineGraphExportOptions ExportOptions() {
        return new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
//            , new DebugMiningDiffNodeFormat()
                , new ReleaseMiningDiffNodeFormat()
                , TaggedPredicate.and(
//                        TaggedPredicate.and(
                            DiffTreeFilter.notEmpty(),
                            DiffTreeFilter.moreThanTwoCodeNodes()
//                        ),
                        /// We want to exclude patches that do not edit variability.
                        /// In particular we noticed that most edits just insert or delete code (or replace it).
                        /// This is reasonable and was also observed in previous studies: Edits to code are more frequent than edits to variability.
                        /// Yet, such edits cannot reveal compositions of more complex edits to variability.
                        /// We thus filter them.
//                        DiffTreeFilter.hasEditsToVariability()
                )
                , Postprocessing()
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

        totalResult.exportTo(outputDir.resolve("totalresult" + DiffTreeMiningResult.EXTENSION));
    }

    public static void mineAsync(
            final Repository repo,
            final Path outputDir,
            final Supplier<DiffTreeLineGraphExportOptions> exportOptions,
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
                    exportOptions.get(),
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
                totalResult.mappend(threadsResult);
                commitSpeedMonitor.addFinishedTasks(threadsResult.exportedCommits);
            }
        } catch (Exception e) {
            Logger.error("Failed to run all mining task!");
            Logger.error(e);
        }
        Logger.info("<<< done in " + clock.printPassedSeconds());

        totalResult.exportTo(outputDir.resolve("totalresult" + DiffTreeMiningResult.EXTENSION));
    }

    public static void main(String[] args) {
        Main.setupLogger(Level.INFO);

        final DebugOptions debugOptions = new DebugOptions(DebugOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF);

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path linuxDir = Paths.get("..", "variantevolution_datasets");
        final Path outputDir = Paths.get("results", "mining");

        final List<Repository> repos = List.of(
                DefaultRepositories.stanciulescuMarlinZip(Path.of("."))
//                DefaultRepositories.createRemoteLinuxRepo(linuxDir.resolve("linux"))
//                DefaultRepositories.createRemoteVimRepo(inputDir.resolve("vim"))
        );

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        for (final Repository repo : repos) {
            Logger.info(" === Begin Processing " + repo.getRepositoryName() + " ===");
            final Clock clock = new Clock();
            clock.start();

            repo.setDebugOptions(debugOptions);
            final Path repoOutputDir = outputDir.resolve(repo.getRepositoryName());
            mineAsync(repo, repoOutputDir, DiffTreeMiner::ExportOptions, DiffTreeMiner::MiningStrategy);
//            mine(repo, repoOutputDir, ExportOptions(), MiningStrategy());

            Logger.info(" === End Processing " + repo.getRepositoryName() + " after " + clock.printPassedSeconds() + " ===");
        }

        Logger.info("Done");
    }
}

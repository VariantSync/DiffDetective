package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.monitoring.TaskCompletionMonitor;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.mining.MiningTask;
import org.variantsync.diffdetective.parallel.ScheduledTasksIterator;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.Diagnostics;
import org.variantsync.diffdetective.util.InvocationCounter;
import org.variantsync.functjonal.iteration.ClusteredIterator;
import org.variantsync.functjonal.iteration.MappedIterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * An analyses that is performed for the entire commit histories of each given git repository.
 * @param repositoriesToAnalyze The repositories whose commit history should be analyzed.
 * @param outputDir The directory to which any produced results should be written.
 * @param commitsToProcessPerThread Number of commits that should be processed by each single thread if multithreading is used.
 *                                  Each thread will be given this number of commits to process.
 *                                  A larger number means fewer threads and less scheduling.
 *                                  A smaller number means more threads but also more scheduling.
 * @param whatToDo A factory for tasks that should be executed for the commits of a certain repository.
 * @param postProcessingOnRepositoryOutputDir A callback that is invoked after all analyses are completed.
 *                                            The argument is the output directory on which postprocessing might occur.
 * @author Paul Bittner
 */
public record HistoryAnalysis(
        List<Repository> repositoriesToAnalyze,
        Path outputDir,
        int commitsToProcessPerThread,
        CommitHistoryAnalysisTaskFactory whatToDo,
        Consumer<Path> postProcessingOnRepositoryOutputDir
) {
    /**
     * File name that is used to store the analysis results for each repository.
     */
    public static final String TOTAL_RESULTS_FILE_NAME = "totalresult" + AnalysisResult.EXTENSION;
    /**
     * Default value for <code>commitsToProcessPerThread</code>
     * @see org.variantsync.diffdetective.analysis.HistoryAnalysis#HistoryAnalysis(List, Path, int, CommitHistoryAnalysisTaskFactory, Consumer) 
     */
    public static final int COMMITS_TO_PROCESS_PER_THREAD_DEFAULT = 1000;

    @Deprecated
    public static void analyze(
            final Repository repo,
            final Path outputDir,
            final ExplainedFilter<DiffTree> treeFilter,
            final List<DiffTreeTransformer> treePreProcessing,
            final LineGraphExportOptions exportOptions,
            final AnalysisStrategy strategy)
    {
        AnalysisResult totalResult;
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
                treeFilter,
                treePreProcessing,
                strategy,
                commitsToProcess
        ), exportOptions);
        Logger.info("Scheduled {} commits.", commitsToProcess.size());
        commitsToProcess = null; // free reference to enable garbage collection
        Logger.info("<<< done after {}", clock.printPassedSeconds());

        Logger.info(">>> Run mining");
        clock.start();
        try {
            totalResult = task.call();
        } catch (Exception e) {
            Logger.error(e);
            Logger.info("<<< aborted after {}", clock.printPassedSeconds());
            return;
        }
        Logger.info("<<< done after {}", clock.printPassedSeconds());

        exportMetadata(outputDir, totalResult);
    }

    /**
     * Static analysis method that can be used without creating an HistoryAnalysis object first.
     * Analyzes the history of the given repository with the given parameters.
     * @param repo The repository to analyze.
     * @param outputDir The directory to which any produced results should be written.
     * @param taskFactory A factory for tasks that should be executed for the commits of a certain repository.
     * @param commitsToProcessPerThread Number of commits that should be processed by each single thread if multithreading is used.
     */
    public static void analyzeAsync(
            final Repository repo,
            final Path outputDir,
            final CommitHistoryAnalysisTaskFactory taskFactory,
            int commitsToProcessPerThread)
    {
        final AnalysisResult totalResult = new AnalysisResult(repo.getRepositoryName());
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        final int nThreads = Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors();
        Logger.info(">>> Scheduling asynchronous analysis on {} threads.", nThreads);
        clock.start();
        final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
        final Iterator<CommitHistoryAnalysisTask> tasks = new MappedIterator<>(
                /// 1.) Retrieve COMMITS_TO_PROCESS_PER_THREAD commits from the differ and cluster them into one list.
                new ClusteredIterator<>(
                        differ.yieldRevCommitsAfter(numberOfTotalCommits),
                        commitsToProcessPerThread
                ),
                /// 2.) Create a MiningTask for the list of commits. This task will then be processed by one
                ///     particular thread.
                commitList -> taskFactory.create(
                        repo,
                        differ,
                        outputDir.resolve(commitList.get(0).getId().getName() + ".lg"),
                        commitList)
        );
        Logger.info("<<< done in {}", clock.printPassedSeconds());

        final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
        Logger.info(">>> Run Analysis");
        clock.start();
        commitSpeedMonitor.start();
        try (final ScheduledTasksIterator<AnalysisResult> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                final AnalysisResult threadsResult = threads.next();
                totalResult.append(threadsResult);
                commitSpeedMonitor.addFinishedTasks(threadsResult.exportedCommits);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to run all mining task");
            System.exit(1);
        }

        final double runtime = clock.getPassedSeconds();
        Logger.info("<<< done in {}", Clock.printPassedSeconds(runtime));

        totalResult.runtimeWithMultithreadingInSeconds = runtime;
        totalResult.totalCommits = numberOfTotalCommits.invocationCount().get();

        exportMetadata(outputDir, totalResult);
    }

    /**
     * Exports the given metadata object to a file named according
     * {@link org.variantsync.diffdetective.analysis.HistoryAnalysis#TOTAL_RESULTS_FILE_NAME} in the given directory.
     * @param outputDir The directory into which the metadata object file should be written.
     * @param metadata The metadata to serialize 
     * @param <T> Type of the metadata.
     */
    public static <T> void exportMetadata(final Path outputDir, final Metadata<T> metadata) {
        exportMetadataToFile(outputDir.resolve(TOTAL_RESULTS_FILE_NAME), metadata);
    }

    /**
     * Exports the given metadata object to the given file. Overwrites existing files.
     * @param outputFile The file to write.
     * @param metadata The metadata to serialize 
     * @param <T> Type of the metadata.
     */
    public static <T> void exportMetadataToFile(final Path outputFile, final Metadata<T> metadata) {
        final String prettyMetadata = metadata.exportTo(outputFile);
        Logger.info("Metadata:\n{}", prettyMetadata);
    }

    /**
     * Runs this analysis asynchronously.
     * Processes each repository sequentially and runs
     * {@link org.variantsync.diffdetective.analysis.HistoryAnalysis#analyzeAsync(Repository, Path, CommitHistoryAnalysisTaskFactory, int)}
     * on each of them.
     */
    public void runAsync() {
        for (final Repository repo : repositoriesToAnalyze) {
            Logger.info(" === Begin Processing {} ===", repo.getRepositoryName());
            final Clock clock = new Clock();
            clock.start();

            final Path repoOutputDir = outputDir.resolve(repo.getRepositoryName());
            /// Don't repeat work we already did:
            if (!Files.exists(repoOutputDir.resolve(TOTAL_RESULTS_FILE_NAME))) {
                analyzeAsync(repo, repoOutputDir, whatToDo, commitsToProcessPerThread);
                postProcessingOnRepositoryOutputDir.accept(repoOutputDir);
            } else {
                Logger.info("  Skipping repository {} because it has already been processed.", repo.getRepositoryName());
            }

            Logger.info(" === End Processing {} after {} ===", repo.getRepositoryName(), clock.printPassedSeconds());
        }
    }
}

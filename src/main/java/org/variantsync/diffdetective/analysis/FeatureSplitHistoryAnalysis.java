package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.monitoring.TaskCompletionMonitor;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record FeatureSplitHistoryAnalysis(
        List<Repository> repositoriesToAnalyze,
        Path outputDir,
        int commitsToProcessPerThread,
        FeatureSplitAnalysisTaskFactory howToExtractFeatures,
        FeatureSplitAnalysisTaskFactory whatToDo,
        Consumer<Path> postProcessingOnRepositoryOutputDir
) {
    public static final String TOTAL_RESULTS_FILE_NAME = "totalresult" + FeatureSplitResult.EXTENSION;
    public static final int COMMITS_TO_PROCESS_PER_THREAD_DEFAULT = 1000;

    public static void analyzeAsync(
            final Repository repo,
            final Path outputDir, 
            final FeatureSplitAnalysisTaskFactory taskFactory,
            int commitsToProcessPerThread,
            FeatureSplitResult totalResult,
            int numOfFeatures
            ) {
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // Select desired features
        Set<String> randomFeatures = new HashSet<>();
        List<String> rndFeatures = new ArrayList(totalResult.totalFeatures.stream().toList());
        Collections.shuffle(rndFeatures);
        randomFeatures.addAll(rndFeatures.subList(0, numOfFeatures));

        // prepare tasks
        final int nThreads = Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors();
        Logger.info(">>> Scheduling asynchronous analysis on {} threads.", nThreads);
        clock.start();
        final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
        final Iterator<FeatureSplitAnalysisTask> tasks = new MappedIterator<>(
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
                        commitList,
                        randomFeatures)
        );
        Logger.info("<<< done in {}", clock.printPassedSeconds());

        final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
        Logger.info(">>> Run Analysis");
        clock.start();
        commitSpeedMonitor.start();
        try (final ScheduledTasksIterator<FeatureSplitResult> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                final FeatureSplitResult threadsResult = threads.next();
                totalResult.append(threadsResult);
                commitSpeedMonitor.addFinishedTasks(threadsResult.exportedCommits);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to run all mining task");
            System.exit(0);
        }

        final double runtime = clock.getPassedSeconds();
        Logger.info("<<< done in {}", Clock.printPassedSeconds(runtime));

        totalResult.runtimeWithMultithreadingInSeconds = runtime;
        totalResult.totalCommits = numberOfTotalCommits.invocationCount().get();

        exportMetadata(outputDir, totalResult);
    }

    // TODO
    public static FeatureSplitResult extractFeaturesAsync(
        final Repository repo,
        final Path outputDir,
        final FeatureSplitAnalysisTaskFactory taskFactory,
        int commitsToProcessPerThread
        ) {
    final FeatureSplitResult totalResult = new FeatureSplitResult(repo.getRepositoryName());
    final GitDiffer differ = new GitDiffer(repo);
    final Clock clock = new Clock();

    // prepare tasks
    final int nThreads = Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors();
    Logger.info(">>> Scheduling asynchronous extraction of features on {} threads.", nThreads);
    clock.start();
    final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
    final Iterator<FeatureSplitAnalysisTask> tasks = new MappedIterator<>(
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
                    commitList,
                    new HashSet<>())
    );
    Logger.info("<<< done in {}", clock.printPassedSeconds());

    final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
    Logger.info(">>> Run Extraction");
    clock.start();
    commitSpeedMonitor.start();
    try (final ScheduledTasksIterator<FeatureSplitResult> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
        while (threads.hasNext()) {
            final FeatureSplitResult threadsResult = threads.next();
            totalResult.append(threadsResult);
            commitSpeedMonitor.addFinishedTasks(threadsResult.exportedCommits);
        }
    } catch (Exception e) {
        Logger.error(e, "Failed to run all mining task");
        System.exit(0);
    }

    final double runtime = clock.getPassedSeconds();
    Logger.info("<<< done in {}", Clock.printPassedSeconds(runtime));

    return totalResult;
}



    public static <T> void exportMetadata(final Path outputDir, final Metadata<T> totalResult) {
        exportMetadataToFile(outputDir.resolve(TOTAL_RESULTS_FILE_NAME), totalResult);
    }

    public static <T> void exportMetadataToFile(final Path outputFile, final Metadata<T> totalResult) {
        final String prettyMetadata = totalResult.exportTo(outputFile);
        Logger.info("Metadata:\n{}", prettyMetadata);
    }

    public void runAsync() {
        for (final Repository repo : repositoriesToAnalyze) {
            Logger.info(" === Begin Processing {} ===", repo.getRepositoryName());
            final Clock clock = new Clock();
            clock.start();

            final Path repoOutputDir = outputDir.resolve(repo.getRepositoryName());
            /// Don't repeat work we already did:
            if (!Files.exists(repoOutputDir.resolve(TOTAL_RESULTS_FILE_NAME))) {
                Logger.info(" === Begin Feature Extraction {} ===", repo.getRepositoryName());
                //TODO Add async
                FeatureSplitResult totalResult = extractFeaturesAsync(repo, outputDir, howToExtractFeatures, commitsToProcessPerThread);


                Logger.info(" === Begin Evaluation {} ===", repo.getRepositoryName());
                analyzeAsync(repo, repoOutputDir, whatToDo, commitsToProcessPerThread, totalResult, 3);
//                mine(repo, repoOutputDir, ExportOptions(repo), MiningStrategy());

                postProcessingOnRepositoryOutputDir.accept(repoOutputDir);
            } else {
                Logger.info("  Skipping repository {} because it has already been processed.", repo.getRepositoryName());
            }

            Logger.info(" === End Processing {} after {} ===", repo.getRepositoryName(), clock.printPassedSeconds());
        }
    }
}

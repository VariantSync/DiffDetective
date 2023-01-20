package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.monitoring.TaskCompletionMonitor;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.metadata.Metadata;
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
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

/**
 * @author Paul Bittner, Benjamin Moosherr
 */
public class Analysis {
    private Analysis() {
    }

    public static final int COMMITS_TO_PROCESS_PER_THREAD_DEFAULT = 1000;
    public static final String TOTAL_RESULTS_FILE_NAME = "totalresult" + AnalysisResult.EXTENSION;

    public static void forEachRepository(
        List<Repository> repositories,
        Path outputDir,
        BiConsumer<Repository, Path> analyzeRepository
    ) {
        for (final Repository repository : repositories) {
            Logger.info(" === Begin Processing {} ===", repository.getRepositoryName());
            final Clock clock = new Clock();
            clock.start();

            final Path repositoryOutputDir = outputDir.resolve(repository.getRepositoryName());
            // Don't repeat work we already did:
            if (!Files.exists(repositoryOutputDir.resolve(TOTAL_RESULTS_FILE_NAME))) {
                analyzeRepository.accept(repository, repositoryOutputDir);
            } else {
                Logger.info("  Skipping repository {} because it has already been processed.",
                    repository.getRepositoryName());
            }

            Logger.info(" === End Processing {} after {} ===",
                repository.getRepositoryName(),
                clock.printPassedSeconds());
        }
    }

    public static <T extends AnalysisResult<T>> T forEachCommit(
            final Repository repository,
            final Path outputDir,
            final AnalysisTaskFactory<T> taskFactory,
            T initialResult
    ) {
        return forEachCommit(
            repository,
            outputDir,
            taskFactory,
            initialResult,
            Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors()
        );
    }

    public static <T extends AnalysisResult<T>> T forEachCommit(
            final Repository repository,
            final Path outputDir,
            final AnalysisTaskFactory<T> taskFactory,
            T initialResult,
            final int nThreads)
    {
        final T totalResult = initialResult;
        final GitDiffer differ = new GitDiffer(repository);
        final Clock clock = new Clock();

        // prepare tasks
        Logger.info(">>> Scheduling asynchronous analysis on {} threads.", nThreads);
        clock.start();
        final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
        final Iterator<Callable<T>> tasks = new MappedIterator<>(
                /// 1.) Retrieve COMMITS_TO_PROCESS_PER_THREAD commits from the differ and cluster them into one list.
                new ClusteredIterator<>(
                        differ.yieldRevCommitsAfter(numberOfTotalCommits),
                        COMMITS_TO_PROCESS_PER_THREAD_DEFAULT
                ),
                /// 2.) Create a MiningTask for the list of commits. This task will then be processed by one
                ///     particular thread.
                commitList -> taskFactory.create(
                        repository,
                        differ,
                        outputDir.resolve(commitList.get(0).getId().getName() + ".lg"),
                        commitList)
        );
        Logger.info("<<< done in {}", clock.printPassedSeconds());

        final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
        Logger.info(">>> Run Analysis");
        clock.start();
        commitSpeedMonitor.start();
        try (final ScheduledTasksIterator<T> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                final T threadsResult = threads.next();
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
        return totalResult;
    }

    /**
     * Exports the given metadata object to a file named according
     * {@link TOTAL_RESULTS_FILE_NAME} in the given directory.
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
}

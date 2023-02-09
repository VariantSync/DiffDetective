package org.variantsync.diffdetective.analysis;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.monitoring.TaskCompletionMonitor;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.parallel.ScheduledTasksIterator;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.Diagnostics;
import org.variantsync.diffdetective.util.InvocationCounter;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.functjonal.iteration.ClusteredIterator;
import org.variantsync.functjonal.iteration.MappedIterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Paul Bittner, Benjamin Moosherr
 */
public class Analysis<T extends AnalysisResult<T>> {
    public static final int COMMITS_TO_PROCESS_PER_THREAD_DEFAULT = 1000;
    public static final String TOTAL_RESULTS_FILE_NAME = "totalresult" + AnalysisResult.EXTENSION;

    protected final List<Hooks<T>> hooks;
    protected final Repository repository;

    protected GitDiffer differ;
    protected RevCommit commit;
    protected CommitDiff commitDiff;
    protected PatchDiff patch;
    protected DiffTree diffTree;

    protected final Path outputDir;
    protected Path outputFile;
    protected final T result;

    public Repository getRepository() {
        return repository;
    }

    public RevCommit getCommit() {
        return commit;
    }

    public CommitDiff getCommitDiff() {
        return commitDiff;
    }

    public PatchDiff getPatch() {
        return patch;
    }

    public DiffTree getDiffTree() {
        return diffTree;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public Path getOutputFile() {
        return outputFile;
    }

    public T getResult() {
        return result;
    }

    public interface Hooks<T extends AnalysisResult<T>> {
        default void beginBatch(Analysis<T> analysis) throws Exception {}
        default boolean beginCommit(Analysis<T> analysis) throws Exception { return true; }
        default boolean onParsedCommit(Analysis<T> analysis) throws Exception { return true; }
        default boolean beginPatch(Analysis<T> analysis) throws Exception { return true; }
        default boolean analyzeDiffTree(Analysis<T> analysis) throws Exception { return true; }
        default void endPatch(Analysis<T> analysis) throws Exception {}
        default void endCommit(Analysis<T> analysis) throws Exception {}
        default void endBatch(Analysis<T> analysis) throws Exception {}
    }

    public static void forEachRepository(
        List<Repository> repositories,
        Path outputDir,
        BiConsumer<Repository, Path> analyzeRepository
    ) {
        for (final Repository repository : repositories) {
            final Path repositoryOutputDir = outputDir.resolve(repository.getRepositoryName());
            // Don't repeat work we already did:
            if (Files.exists(repositoryOutputDir.resolve(TOTAL_RESULTS_FILE_NAME))) {
                Logger.info("  Skipping repository {} because it has already been processed.",
                    repository.getRepositoryName());
            } else {
                Logger.info(" === Begin Processing {} ===", repository.getRepositoryName());
                final Clock clock = new Clock();
                clock.start();

                analyzeRepository.accept(repository, repositoryOutputDir);

                Logger.info(" === End Processing {} after {} ===",
                    repository.getRepositoryName(),
                    clock.printPassedSeconds());
            }
        }
    }

    public static <T extends AnalysisResult<T>> T forEachCommit(Supplier<Analysis<T>> analysis) {
        return forEachCommit(
            analysis,
            Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors()
        );
    }

    public static <T extends AnalysisResult<T>> T forEachCommit(
        Supplier<Analysis<T>> analysisFactory,
        final int nThreads
    ) {
        var analysis = analysisFactory.get();
        analysis.differ = new GitDiffer(analysis.getRepository());

        final Clock clock = new Clock();

        // prepare tasks
        Logger.info(">>> Scheduling asynchronous analysis on {} threads.", nThreads);
        clock.start();
        final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
        final Iterator<Callable<T>> tasks = new MappedIterator<>(
                /// 1.) Retrieve COMMITS_TO_PROCESS_PER_THREAD commits from the differ and cluster them into one list.
                new ClusteredIterator<>(
                        analysis.differ.yieldRevCommitsAfter(numberOfTotalCommits),
                        COMMITS_TO_PROCESS_PER_THREAD_DEFAULT
                ),
                /// 2.) Create a MiningTask for the list of commits. This task will then be processed by one
                ///     particular thread.
                commitList -> () -> {
                    var threadAnalysis = analysisFactory.get();
                    threadAnalysis.differ = analysis.differ;
                    return threadAnalysis.processCommitBatch(commitList);
                }
        );
        Logger.info("<<< done in {}", clock.printPassedSeconds());

        final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
        Logger.info(">>> Run Analysis");
        clock.start();
        commitSpeedMonitor.start();
        try (final ScheduledTasksIterator<T> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                final T threadsResult = threads.next();
                analysis.getResult().append(threadsResult);
                commitSpeedMonitor.addFinishedTasks(threadsResult.exportedCommits);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to run all mining task");
            System.exit(1);
        }

        final double runtime = clock.getPassedSeconds();
        Logger.info("<<< done in {}", Clock.printPassedSeconds(runtime));

        analysis.getResult().runtimeWithMultithreadingInSeconds = runtime;
        analysis.getResult().totalCommits = numberOfTotalCommits.invocationCount().get();

        exportMetadata(analysis.getOutputDir(), analysis.getResult());
        return analysis.getResult();
    }

    public void processCommits(List<RevCommit> commits) throws Exception {
        differ = new GitDiffer(getRepository());
        processCommitBatch(commits);
    }

    public Analysis(
        List<Hooks<T>> hooks,
        Repository repository,
        Path outputDir,
        T initialResult
    ) {
        this.hooks = hooks;
        this.repository = repository;
        this.outputDir = outputDir;
        this.result = initialResult;

        this.result.repoName = repository.getRepositoryName();
    }

    protected T processCommitBatch(List<RevCommit> commits) throws Exception {
        outputFile = outputDir.resolve(commits.get(0).getId().getName() + ".lg");

        ListIterator<Hooks<T>> batchHook = hooks.listIterator();
        try {
            result.putCustomInfo(MetadataKeys.TASKNAME, this.getClass().getName());
            runHook(batchHook, Hooks::beginBatch);

            // For each commit
            for (final RevCommit finalCommit : commits) {
                commit = finalCommit;

                ListIterator<Hooks<T>> commitHook = hooks.listIterator();
                try {
                    if (!runFilterHook(commitHook, Hooks::beginCommit)) {
                        continue;
                    }

                    processCommit();
                } catch (Exception e) {
                    Logger.error(e, "An unexpected error occurred at {} in {}", commit.getId().getName(), repository.getRepositoryName());
                    throw e;
                } finally {
                    runReverseHook(commitHook, Hooks::endCommit);
                }
            }
        } finally {
            runReverseHook(batchHook, Hooks::endBatch);
        }

        return result;
    }

    protected void processCommit() throws Exception {
        // parse the commit
        final CommitDiffResult commitDiffResult = differ.createCommitDiff(commit);

        // report any errors that occurred and exit in case no DiffTree could be parsed.
        result.reportDiffErrors(commitDiffResult.errors());
        if (commitDiffResult.diff().isEmpty()) {
            Logger.debug("found commit that failed entirely because:\n{}", commitDiffResult.errors());
            ++result.failedCommits;
            return;
        }

        // extract the produced commit diff and inform the strategy
        commitDiff = commitDiffResult.diff().get();
        if (!runFilterHook(hooks.listIterator(), Hooks::onParsedCommit)) {
            return;
        }

        // inspect every patch
        for (final PatchDiff finalPatch : commitDiff.getPatchDiffs()) {
            patch = finalPatch;

            ListIterator<Hooks<T>> patchHook = hooks.listIterator();
            try {
                if (!runFilterHook(patchHook, Hooks::beginPatch)) {
                    continue;
                }

                processPatch();
            } finally {
                runReverseHook(patchHook, Hooks::endPatch);
            }
        }
    }

    protected void processPatch() throws Exception {
        if (patch.isValid()) {
            // generate TreeDiff
            diffTree = patch.getDiffTree();
            diffTree.assertConsistency();

            runFilterHook(hooks.listIterator(), Hooks::analyzeDiffTree);
        }
    }

    protected <Hook> void runHook(ListIterator<Hook> hook, FailableBiConsumer<Hook, Analysis<T>, Exception> callHook) throws Exception {
        while (hook.hasNext()) {
            callHook.accept(hook.next(), this);
        }
    }

    protected <Hook> boolean runFilterHook(ListIterator<Hook> hook, FailableBiFunction<Hook, Analysis<T>, Boolean, Exception> callHook) throws Exception {
        while (hook.hasNext()) {
            if (!callHook.apply(hook.next(), this)) {
                return false;
            }
        }

        return true;
    }

    protected <Hook> void runReverseHook(ListIterator<Hook> hook, FailableBiConsumer<Hook, Analysis<T>, Exception> callHook) throws Exception {
        Exception catchedException = null;
        while (hook.hasPrevious()) {
            try {
                callHook.accept(hook.previous(), this);
            } catch (Exception e) {
                Logger.error(e, "An exception thrown in an end hooks of Analysis will be rethrown later");
                catchedException = e;
            }
        }

        if (catchedException != null) {
            throw catchedException;
        }
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

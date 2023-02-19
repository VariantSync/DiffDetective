package org.variantsync.diffdetective.analysis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
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

/**
 * Encapsulates the state and control flow during an analysis of the commit history of multiple
 * repositories using {@link DiffTree}s. Each repository is processed sequentially but the commits
 * of each repository can be processed in parallel.
 *
 * <p>For thread safety, each thread receives its own instance of {@code Analysis}. The getters
 * provides access to the current state of the analysis in one thread. Depending on the current
 * {@link Hooks phase} only a subset of the state accessible via getters may be valid.
 *
 * @see forEachRepository
 * @see forEachCommit
 * @author Paul Bittner, Benjamin Moosherr
 */
public class Analysis {
    /**
     * File extension that is used when writing AnalysisResults to disk.
     */
    public static final String EXTENSION = ".metadata.txt";
    /**
     * File name that is used to store the analysis results for each repository.
     */
    public static final String TOTAL_RESULTS_FILE_NAME = "totalresult" + EXTENSION;
    /**
     * Default value for <code>commitsToProcessPerThread</code>
     * @see forEachCommit(Supplier, int, int)
     */
    public static final int COMMITS_TO_PROCESS_PER_THREAD_DEFAULT = 1000;

    protected final List<Hooks> hooks;
    protected final Repository repository;

    protected GitDiffer differ;
    protected RevCommit currentCommit;
    protected CommitDiff currentCommitDiff;
    protected PatchDiff currentPatch;
    protected DiffTree currentDiffTree;

    protected final Path outputDir;
    protected Path outputFile;
    protected final AnalysisResult result;

    /**
     * The repository this analysis is run on.
     * Always valid.
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * The currently processed commit.
     * Valid during the commit {@link Hooks phase}.
     */
    public RevCommit getCurrentCommit() {
        return currentCommit;
    }

    /**
     * The currently processed commit diff.
     * Valid when {@link Hooks#onParsedCommit} is called until the end of the commit phase.
     */
    public CommitDiff getCurrentCommitDiff() {
        return currentCommitDiff;
    }

    /**
     * The currently processed patch.
     * Valid during the patch {@link Hooks phase}.
     */
    public PatchDiff getCurrentPatch() {
        return currentPatch;
    }

    /**
     * The currently processed patch.
     * Valid only during {@link Hooks#analyzeDiffTree}.
     */
    public DiffTree getCurrentDiffTree() {
        return currentDiffTree;
    }

    /**
     * The destination for results which are written to disk.
     * Always valid.
     */
    public Path getOutputDir() {
        return outputDir;
    }

    /**
     * The destination for results which are written to disk and specific to the currently processed
     * commit batch.
     * Valid during the batch {@link Hooks phase}.
     */
    public Path getOutputFile() {
        return outputFile;
    }

    /**
     * The results of the analysis. This may be modified by any hook and should be initialized in
     * {@link Hooks#initializeResults} (e.g. by using {@link append}).
     * Always valid.
     */
    public AnalysisResult getResult() {
        return result;
    }

    /**
     * Convenience getter for {@link AnalysisResult#get} on {@link getResult}.
     * Always valid.
     */
    public <T extends Metadata<T>> T get(ResultKey<T> resultKey) {
        return result.get(resultKey);
    }

    /**
     * Convenience function for {@link AnalysisResult#append} on {@link getResult}.
     * Always valid.
     */
    public <T extends Metadata<T>> void append(ResultKey<T> resultKey, T value) {
        result.append(resultKey, value);
    }

    /**
     * Hooks for analyzing commits using {@link DiffTree}s.
     *
     * <p>In general the hooks of different {@code Hook} instances are called in sequence according
     * to the order specified in {@link Analysis#Analysis} (except end hooks). Hooks are separated
     * into two categories: phases and events.
     *
     * <p>A phase consists of two hooks with the prefix {@code begin} and {@code end}. It is
     * guaranteed that the end hook is called if and only if the begin hook was called, even in the
     * presence of exceptions, so they are safe to use for resource management. For this purpose,
     * end hooks are called in reverse order as specified in {@link Analysis#Analysis}.
     *
     * <p>Phases can be called an arbitrary number of times but are nested in the following order
     * (from outer to inner):
     * <ul>
     * <li>batch
     * <li>commit
     * <li>patch
     * </ul>
     * An inner phase is only executed while an outer phase runs (in between the phase's begin and
     * end hooks).
     *
     * <p>An analysis implementing {@code Hooks} can perform various actions during each hook. This
     * includes the {@link append creation} and {@link get modification} of {@link getResult
     * analysis results}, modifying their internal state, performing IO operations and throwing
     * exceptions. In contrast, the only analysis state hooks are allowed to modify is the {@link
     * getResult result} of an {@link Analysis}. All other state (e.g. {@link getCurrentCommit})
     * must not be modified. Care must be taken to avoid the reliance of the internal state on a
     * specific commit batch being processed as only the {@link getResult results} of each commit
     * batch are merged and returned by {@link forEachCommit}.
     *
     * <p>Hooks that return a {@code boolean} are called filter hooks and can, in addition to the
     * above, skip any further processing in the current phase (including following inner phases) by
     * returning {@code false}. If a hook starts skipping, any invocations of the same filter hook
     * of following {@code Hook} instances won't be executed. Processing continues (after calling
     * missing end hooks of the current phase) in the next outer phase after the skipped phase.
     *
     * <p>Hooks without a {@code begin} or {@code end} prefix are events emitted during some
     * specified conditions. See their respective documentation for details.
     */
    public interface Hooks {
        /**
         * Initialization hook for {@link getResult}. All result types should be appended with a
         * neutral value using {@link append}. No other side effects should be performed during this
         * methods as it might be called an arbitrary amount of times.
         */
        default void initializeResults(Analysis analysis) {}
        default void beginBatch(Analysis analysis) throws Exception {}
        default boolean beginCommit(Analysis analysis) throws Exception { return true; }
        /**
         * Signals a parsing failure of some patch in the current commit.
         * Called at most once during the commit phase. If this hook is called {@link
         * onParsedCommit} and the following patch phase invocations are skipped.
         */
        default void onFailedCommit(Analysis analysis) throws Exception {}
        /**
         * Signals the completion of the commit diff extraction.
         * Called exactly once during the commit phase before the patch phase begins.
         */
        default boolean onParsedCommit(Analysis analysis) throws Exception { return true; }
        default boolean beginPatch(Analysis analysis) throws Exception { return true; }
        /**
         * The main hook for analyzing non-empty diff trees.
         * Called at most once during the patch phase.
         */
        default boolean analyzeDiffTree(Analysis analysis) throws Exception { return true; }
        default void endPatch(Analysis analysis) throws Exception {}
        default void endCommit(Analysis analysis) throws Exception {}
        default void endBatch(Analysis analysis) throws Exception {}
    }

    /**
     * Runs {@code analyzeRepository} on each repository, skipping repositories where an analysis
     * was already run. This skipping mechanism doesn't distinguish between different analyses as it
     * only checks for the existence of {@link TOTAL_RESULTS_FILE_NAME}. Delete this file to rerun
     * the analysis.
     *
     * For each repository a directory in {@code outputDir} is passed to {@code analyzeRepository}
     * where the results of the given repository should be written.
     *
     * @param repositoriesToAnalyze the repositories for which {@code analyzeRepository} is run
     * @param outputDir the directory where all repositories will save their results
     * @param analyzeRepository the callback which is invoked for each repository
     */
    public static void forEachRepository(
        List<Repository> repositoriesToAnalyze,
        Path outputDir,
        BiConsumer<Repository, Path> analyzeRepository
    ) {
        for (final Repository repo : repositoriesToAnalyze) {
            final Path repoOutputDir = outputDir.resolve(repo.getRepositoryName());
            // Don't repeat work we already did:
            if (Files.exists(repoOutputDir.resolve(TOTAL_RESULTS_FILE_NAME))) {
                Logger.info("  Skipping repository {} because it has already been processed.",
                    repo.getRepositoryName());
            } else {
                Logger.info(" === Begin Processing {} ===", repo.getRepositoryName());
                final Clock clock = new Clock();
                clock.start();

                analyzeRepository.accept(repo, repoOutputDir);

                Logger.info(" === End Processing {} after {} ===",
                    repo.getRepositoryName(),
                    clock.printPassedSeconds());
            }
        }
    }

    /**
     * Same as {@link forEachCommit(Supplier<Analysis>, int, int)}.
     * Defaults to {@link COMMITS_TO_PROCESS_PER_THREAD_DEFAULT} and a machine dependent number of
     * {@link Diagnostics#getNumberOfAvailableProcessors}.
     */
    public static AnalysisResult forEachCommit(Supplier<Analysis> analysis) {
        return forEachCommit(
            analysis,
            COMMITS_TO_PROCESS_PER_THREAD_DEFAULT,
            Diagnostics.INSTANCE.run().getNumberOfAvailableProcessors()
        );
    }

    /**
     * Runs the analysis for the repository given in {@link Analysis#Analysis}. The repository
     * history is processed in batches of {@code commitsToProcessPerThread} on {@code nThreads} in
     * parallel. {@link Hooks} passed to {@link Analysis#Analysis} are the main customization point
     * for executing different analyses. By default only the total number of commits and the total
     * runtime with multithreading of the {@link DiffTree} parsing is recorded.
     *
     * @param analysisFactory creates independent (at least thread safe) instances the analysis
     * state
     * @param commitsToProcessPerThread the commit batch size
     * @param nThreads the number of parallel processed commit batches
     */
    public static AnalysisResult forEachCommit(
        Supplier<Analysis> analysisFactory,
        final int commitsToProcessPerThread,
        final int nThreads
    ) {
        var analysis = analysisFactory.get();
        analysis.differ = new GitDiffer(analysis.getRepository());

        final Clock clock = new Clock();

        // prepare tasks
        Logger.info(">>> Scheduling asynchronous analysis on {} threads.", nThreads);
        clock.start();
        final InvocationCounter<RevCommit, RevCommit> numberOfTotalCommits = InvocationCounter.justCount();
        final Iterator<Callable<AnalysisResult>> tasks = new MappedIterator<>(
                /// 1.) Retrieve COMMITS_TO_PROCESS_PER_THREAD commits from the differ and cluster them into one list.
                new ClusteredIterator<>(
                        analysis.differ.yieldRevCommitsAfter(numberOfTotalCommits),
                        commitsToProcessPerThread
                ),
                /// 2.) Create a MiningTask for the list of commits. This task will then be processed by one
                ///     particular thread.
                commitList -> () -> analysisFactory.get().processCommits(commitList, analysis.differ)
        );
        Logger.info("<<< done in {}", clock.printPassedSeconds());

        final TaskCompletionMonitor commitSpeedMonitor = new TaskCompletionMonitor(0, TaskCompletionMonitor.LogProgress("commits"));
        Logger.info(">>> Run Analysis");
        clock.start();
        commitSpeedMonitor.start();
        try (final ScheduledTasksIterator<AnalysisResult> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                final AnalysisResult threadsResult = threads.next();
                analysis.getResult().append(threadsResult);

                var statistics = threadsResult.get(StatisticsAnalysis.RESULT);
                if (statistics != null) {
                    commitSpeedMonitor.addFinishedTasks(statistics.processedCommits);
                }
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

    /**
     * Constructs the state used during an analysis.
     *
     * @param taskName the name of the overall analysis task
     * @param hooks the hooks to be run for analysis
     * @param repository the repository to analyze
     * @param outputDir the directory where all results are saved
     */
    public Analysis(
        String taskName,
        List<Hooks> hooks,
        Repository repository,
        Path outputDir
    ) {
        this.hooks = hooks;
        this.repository = repository;
        this.outputDir = outputDir;
        this.result = new AnalysisResult();

        this.result.repoName = repository.getRepositoryName();
        this.result.taskName = taskName;
        for (var hook : hooks) {
            hook.initializeResults(this);
        }
    }

    /**
     * Entry point into a sequential analysis of {@code commits} as one batch.
     * Same as {@link processCommits(List<RevCommit>, GitDiffer)} with a default {@link GitDiffer}.
     *
     * @param commits the commit batch to be processed
     * @see forEachCommit
     */
    public AnalysisResult processCommits(List<RevCommit> commits) throws Exception {
        return processCommits(commits, new GitDiffer(getRepository()));
    }

    /**
     * Entry point into a sequential analysis of {@code commits} as one batch.
     *
     * @param commits the commit batch to be processed
     * @param differ the differ to use
     * @see forEachCommit
     */
    public AnalysisResult processCommits(List<RevCommit> commits, GitDiffer differ) throws Exception {
        this.differ = differ;
        processCommitBatch(commits);
        return getResult();
    }

    protected void processCommitBatch(List<RevCommit> commits) throws Exception {
        outputFile = outputDir.resolve(commits.get(0).getId().getName() + ".lg");

        ListIterator<Hooks> batchHook = hooks.listIterator();
        try {
            runHook(batchHook, Hooks::beginBatch);

            // For each commit
            for (final RevCommit finalCommit : commits) {
                currentCommit = finalCommit;

                ListIterator<Hooks> commitHook = hooks.listIterator();
                try {
                    if (!runFilterHook(commitHook, Hooks::beginCommit)) {
                        continue;
                    }

                    processCommit();
                } catch (Exception e) {
                    Logger.error(e, "An unexpected error occurred at {} in {}", currentCommit.getId().getName(), repository.getRepositoryName());
                    throw e;
                } finally {
                    runReverseHook(commitHook, Hooks::endCommit);
                }
            }
        } finally {
            runReverseHook(batchHook, Hooks::endBatch);
        }
    }

    protected void processCommit() throws Exception {
        // parse the commit
        final CommitDiffResult commitDiffResult = differ.createCommitDiff(currentCommit);

        // report any errors that occurred and exit in case no DiffTree could be parsed.
        getResult().reportDiffErrors(commitDiffResult.errors());
        if (commitDiffResult.diff().isEmpty()) {
            Logger.debug("found commit that failed entirely because:\n{}", commitDiffResult.errors());
            runHook(hooks.listIterator(), Hooks::onFailedCommit);
            return;
        }

        // extract the produced commit diff and inform the strategy
        currentCommitDiff = commitDiffResult.diff().get();
        if (!runFilterHook(hooks.listIterator(), Hooks::onParsedCommit)) {
            return;
        }

        // inspect every patch
        for (final PatchDiff finalPatch : currentCommitDiff.getPatchDiffs()) {
            currentPatch = finalPatch;

            ListIterator<Hooks> patchHook = hooks.listIterator();
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
        if (currentPatch.isValid()) {
            // generate TreeDiff
            currentDiffTree = currentPatch.getDiffTree();
            currentDiffTree.assertConsistency();

            runFilterHook(hooks.listIterator(), Hooks::analyzeDiffTree);
        }
    }

    protected <Hook> void runHook(ListIterator<Hook> hook, FailableBiConsumer<Hook, Analysis, Exception> callHook) throws Exception {
        while (hook.hasNext()) {
            callHook.accept(hook.next(), this);
        }
    }

    protected <Hook> boolean runFilterHook(ListIterator<Hook> hook, FailableBiFunction<Hook, Analysis, Boolean, Exception> callHook) throws Exception {
        while (hook.hasNext()) {
            if (!callHook.apply(hook.next(), this)) {
                return false;
            }
        }

        return true;
    }

    protected <Hook> void runReverseHook(ListIterator<Hook> hook, FailableBiConsumer<Hook, Analysis, Exception> callHook) throws Exception {
        Exception catchedException = null;
        while (hook.hasPrevious()) {
            try {
                callHook.accept(hook.previous(), this);
            } catch (Exception e) {
                Logger.error(e, "An exception thrown in an end hooks of Analysis will be rethrown later");
                if (catchedException == null) {
                    catchedException = e;
                } else {
                    catchedException.addSuppressed(e);
                }
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

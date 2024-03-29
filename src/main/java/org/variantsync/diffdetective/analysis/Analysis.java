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
import org.variantsync.diffdetective.util.*;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.functjonal.iteration.ClusteredIterator;
import org.variantsync.functjonal.iteration.MappedIterator;

/**
 * Encapsulates the state and control flow during an analysis of the commit history of multiple
 * repositories using {@link VariationDiff}s. Each repository is processed sequentially but the commits
 * of each repository can be processed in parallel.
 *
 * <p>For thread safety, each thread receives its own instance of {@code Analysis}. The getters
 * provides access to the current state of the analysis in one thread. Depending on the current
 * {@link Hooks phase} only a subset of the state accessible via getters may be valid.
 *
 * @see #forEachRepository
 * @see #forEachCommit
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
     * @see #forEachCommit(Supplier, int, int)
     */
    public static final int COMMITS_TO_PROCESS_PER_THREAD_DEFAULT = 1000;

    protected final List<Hooks> hooks;
    protected final Repository repository;

    protected GitDiffer differ;
    protected RevCommit currentCommit;
    protected CommitDiff currentCommitDiff;
    protected PatchDiff currentPatch;
    protected VariationDiff<DiffLinesLabel> currentVariationDiff;

    protected final Path outputDir;
    protected Path outputFile;
    protected final AnalysisResult result;

    /**
     * The total number of commits in the observed history of the given repository.
     */
    public final static class TotalNumberOfCommitsResult extends SimpleMetadata<Integer, TotalNumberOfCommitsResult> {
        public final static ResultKey<TotalNumberOfCommitsResult> KEY = new ResultKey<>(TotalNumberOfCommitsResult.class.getName());

        public TotalNumberOfCommitsResult() {
            super(
                    0,
                    MetadataKeys.TOTAL_COMMITS,
                    Integer::sum,
                    Integer::parseInt
            );
        }
    }

    /**
     * The effective runtime in seconds that we have when using multithreading.
     */
    public final static class RuntimeWithMultithreadingResult extends SimpleMetadata<Double, RuntimeWithMultithreadingResult> {
        public final static ResultKey<RuntimeWithMultithreadingResult> KEY = new ResultKey<>(RuntimeWithMultithreadingResult.class.getName());

        public RuntimeWithMultithreadingResult() {
            super(
                    0.0,
                    MetadataKeys.RUNTIME_WITH_MULTITHREADING,
                    Double::sum,
                    Double::parseDouble
            );
        }
    }

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
     * Valid only during {@link Hooks#analyzeVariationDiff}.
     */
    public VariationDiff<DiffLinesLabel> getCurrentVariationDiff() {
        return currentVariationDiff;
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
     * {@link Hooks#initializeResults} (e.g. by using {@link #append}).
     * Always valid.
     */
    public AnalysisResult getResult() {
        return result;
    }

    /**
     * Convenience getter for {@link AnalysisResult#get} on {@link #getResult}.
     * Always valid.
     */
    public <T extends Metadata<T>> T get(ResultKey<T> resultKey) {
        return result.get(resultKey);
    }

    /**
     * Convenience function for {@link AnalysisResult#append} on {@link #getResult}.
     * Always valid.
     */
    public <T extends Metadata<T>> void append(ResultKey<T> resultKey, T value) {
        result.append(resultKey, value);
    }

    /**
     * Hooks for analyzing commits using {@link VariationDiff}s.
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
     * includes the {@link #append creation} and {@link #get modification} of {@link #getResult
     * analysis results}, modifying their internal state, performing IO operations and throwing
     * exceptions. In contrast, the only analysis state hooks are allowed to modify is the {@link
     * #getResult result} of an {@link Analysis}. All other state (e.g. {@link #getCurrentCommit})
     * must not be modified. Care must be taken to avoid the reliance of the internal state on a
     * specific commit batch being processed as only the {@link #getResult results} of each commit
     * batch are merged and returned by {@link #forEachCommit}.
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
         * Initialization hook for {@link #getResult}. All result types should be appended with a
         * neutral value using {@link #append}. No other side effects should be performed during this
         * methods as it might be called an arbitrary amount of times.
         */
        default void initializeResults(Analysis analysis) {}
        default void beginBatch(Analysis analysis) throws Exception {}
        default boolean beginCommit(Analysis analysis) throws Exception { return true; }
        /**
         * Signals a parsing failure of all patches in the current commit.
         * Called at most once during the commit phase. If this hook is called {@link
         * #onParsedCommit} and the following patch phase invocations are skipped.
         */
        default void onFailedCommit(Analysis analysis) throws Exception {}
        /**
         * Signals a parsing failure of some patch in the current commit.
         * Called at most once during the commit phase.
         */
        default void onFailedParse(Analysis analysis) throws Exception {}
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
        default boolean analyzeVariationDiff(Analysis analysis) throws Exception { return true; }
        default void endPatch(Analysis analysis) throws Exception {}
        default void endCommit(Analysis analysis) throws Exception {}
        default void endBatch(Analysis analysis) throws Exception {}
    }

    /**
     * Runs {@code analyzeRepository} on each repository, skipping repositories where an analysis
     * was already run. This skipping mechanism doesn't distinguish between different analyses as it
     * only checks for the existence of {@link #TOTAL_RESULTS_FILE_NAME}. Delete this file to rerun
     * the analysis.
     * <p>
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
     * Runs the analysis for the repository given in {@link Analysis#Analysis} on the given commit only.
     * {@link Hooks} passed to {@link Analysis#Analysis} are the main customization point
     * for executing different analyses.
     *
     * @param commitHash the commit to analyze relative to its first parent
     * @param analysis the analysis to run
     */
    public static AnalysisResult forSingleCommit(final String commitHash, final Analysis analysis) {
        analysis.differ = new GitDiffer(analysis.getRepository());

        final Clock clock = new Clock();
        // prepare tasks
        Logger.info(">>> Running Analysis on single commit {} in {}", commitHash, analysis.getRepository().getRepositoryName());
        clock.start();

        AnalysisResult result = null;
        try {
            final RevCommit commit = analysis.differ.getCommit(commitHash);
            analysis.processCommitBatch(List.of(commit));
            result = analysis.getResult();
        } catch (Exception e) {
            Logger.error("Failed to analyze {}. Exiting.", commitHash);
            System.exit(1);
        }

        final double runtime = clock.getPassedSeconds();
        Logger.info("<<< done in {}", Clock.printPassedSeconds(runtime));
        
        result.get(TotalNumberOfCommitsResult.KEY).value++;

        exportMetadata(analysis.getOutputDir(), result);
        return result;
    }

    /**
     * Runs the analysis for the repository given in {@link Analysis#Analysis} on the given patch only.
     * {@link Hooks} passed to {@link Analysis#Analysis} are the main customization point
     * for executing different analyses.
     * The Hooks will be manipulated in that a new hook for patch filtering will be inserted as the first hook
     * for as long as the analysis runs. This hook will be removed afterwards. It is assumed that this hook
     * remains at the same place and is not manipulated by the user.
     *
     * @param commitHash the commit to analyze relative to its first parent
     * @param fileName the name of the file that was edited in the given commit
     * @param analysis the analysis to run
     */
    public static void forSinglePatch(final String commitHash, final String fileName, final Analysis analysis) {
        assert fileName != null;

        final Hooks filterPatchHook = new Hooks() {
            @Override
            public boolean beginPatch(Analysis analysis) {
                return fileName.equals(analysis.getCurrentPatch().getFileName(Time.AFTER))
                        || fileName.equals(analysis.getCurrentPatch().getFileName(Time.BEFORE));
            }
        };

        // Add a hook that skips all patches unequal to the requested one.
        analysis.hooks.add(0, filterPatchHook);
        forSingleCommit(commitHash, analysis);

        // Assert that our hook is still in place after the analysis ...
        Assert.assertTrue(analysis.hooks.isEmpty() || analysis.hooks.get(0) == filterPatchHook);
        // ... and remove it.
        analysis.hooks.remove(0);
    }

    /**
     * Same as {@link #forEachCommit(Supplier, int, int)}.
     * Defaults to {@link #COMMITS_TO_PROCESS_PER_THREAD_DEFAULT} and a machine dependent number of
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
     * runtime with multithreading of the {@link VariationDiff} parsing is recorded.
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
        analysis.result.append(RuntimeWithMultithreadingResult.KEY, new RuntimeWithMultithreadingResult());

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
                commitList -> () -> {
                    Analysis thisThreadsAnalysis = analysisFactory.get();
                    thisThreadsAnalysis.differ = analysis.differ;
                    thisThreadsAnalysis.processCommitBatch(commitList);
                    return thisThreadsAnalysis.getResult();
                }
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

        analysis.getResult().get(RuntimeWithMultithreadingResult.KEY).value = runtime;
//        analysis.getResult().get(TotalNumberOfCommitsResult.KEY).value = numberOfTotalCommits.invocationCount().get();

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
        
        this.result = new AnalysisResult(repository.getRepositoryName());
        this.result.taskName = taskName;
        
        for (var hook : hooks) {
            hook.initializeResults(this);
        }
    }

    /**
     * Sequential analysis of all {@code commits} as one batch.
     *
     * @param commits the commit batch to be processed
     * @see #forEachCommit
     */
    protected void processCommitBatch(List<RevCommit> commits) throws Exception {
        outputFile = outputDir.resolve(commits.get(0).getId().getName());

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

            // export the thread's result
            getResult().exportTo(FileUtils.addExtension(outputFile, Analysis.EXTENSION));
        }
    }

    protected void processCommit() throws Exception {
        // parse the commit
        final CommitDiffResult commitDiffResult = differ.createCommitDiff(currentCommit);

        // report any errors that occurred and exit in case no VariationDiff could be parsed.
        getResult().reportDiffErrors(commitDiffResult.errors());
        if (!commitDiffResult.errors().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            commitDiffResult.errors().forEach(e -> sb.append(e).append("\n"));
            Logger.debug("found commit for which at least one patch could not be parsed because:\n{}", sb);
            runHook(hooks.listIterator(), Hooks::onFailedParse);
        }
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
            } catch (Throwable t) {
                Logger.error("error during {} {}", currentPatch.getFileName(Time.AFTER), currentPatch.getCommitHash());
                throw t;
            } finally {
                runReverseHook(patchHook, Hooks::endPatch);
            }
        }
        
        getResult().get(TotalNumberOfCommitsResult.KEY).value++;
    }

    protected void processPatch() throws Exception {
        if (currentPatch.isValid()) {
            // generate TreeDiff
            currentVariationDiff = currentPatch.getVariationDiff();
            currentVariationDiff.assertConsistency();

            runFilterHook(hooks.listIterator(), Hooks::analyzeVariationDiff);
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
     * {@link #TOTAL_RESULTS_FILE_NAME} in the given directory.
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

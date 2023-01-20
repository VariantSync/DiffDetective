package org.variantsync.diffdetective.analysis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.category.Semigroup;
import org.variantsync.functjonal.map.MergeMap;

/**
 * The result of a {@link Analysis}.
 * This result stores various metadata and statistics that we use for the validation of our ESEC/FSE paper.
 * An AnalysisResult also allows to store any custom metadata or information.
 * @author Paul Bittner
 */
public abstract class AnalysisResult<T> implements Metadata<T> {
    /**
     * Placeholder name for data that is not associated to a repository or where the repository is unknown.
     */
    public final static String NO_REPO = "<NONE>";

    /**
     * File extension that is used when writing AnalysisResults to disk.
     */
    public final static String EXTENSION = ".metadata.txt";

    public final static String ERROR_BEGIN = "#Error[";
    public final static String ERROR_END = "]";

    /**
     * Inplace semigroup for AnalysisResult.
     * Merges the second results values into the first result.
     */
    public final static <T> InplaceSemigroup<AnalysisResult<T>> ISEMIGROUP() {
        return (a, b) -> {
            a.totalCommits += b.totalCommits;
            a.exportedCommits += b.exportedCommits;
            a.emptyCommits += b.emptyCommits;
            a.failedCommits += b.failedCommits;
            a.exportedTrees += b.exportedTrees;
            a.runtimeInSeconds += b.runtimeInSeconds;
            a.runtimeWithMultithreadingInSeconds += b.runtimeWithMultithreadingInSeconds;
            a.min.set(CommitProcessTime.min(a.min, b.min));
            a.max.set(CommitProcessTime.max(a.max, b.max));
            a.debugData.append(b.debugData);
            MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
            a.diffErrors.append(b.diffErrors);
        };
    };

    public String repoName;
    /**
     * Total number of commits in the observed history.
     */
    public int totalCommits;
    /**
     * Number of processed commits in the observed history.
     * {@code exportedCommits <= totalCommits}
     */
    public int exportedCommits;
    /**
     * Number of commits that were not processed because they had no DiffTrees.
     * A commit is empty iff at least of one of the following conditions is met for every of its patches:
     * - the patch did not edit a C file,
     * - the DiffTree became empty after transformations (this can happen if there are only whitespace changes),
     * - or the patch had syntax errors in its annotations, so the DiffTree could not be parsed.
     */
    public int emptyCommits;
    /**
     * Number of commits that could not be parsed at all because of exceptions when operating JGit.
     *
     * The number of commits that were filtered because they are a merge commit is thus given as
     * totalCommits - exportedCommits - emptyCommits - failedCommits
     */
    public int failedCommits;
    public int exportedTrees;
    public double runtimeInSeconds;
    public double runtimeWithMultithreadingInSeconds;
    public final CommitProcessTime min, max;
    public final DiffTreeSerializeDebugData debugData;
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    public final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    /**
     * Creates an empty analysis result.
     */
    public AnalysisResult() {
        this(NO_REPO);
    }

    /**
     * Creates an empty analysis result for the given repo.
     * @param repoName The repo for which to collect results.
     */
    public AnalysisResult(final String repoName) {
        this(
                repoName,
                0, 0, 0, 0,
                0,
                0, 0,
                CommitProcessTime.Unknown(repoName, Long.MAX_VALUE),
                CommitProcessTime.Unknown(repoName, Long.MIN_VALUE),
                new DiffTreeSerializeDebugData());
    }

    /**
     * Creates am analysis result with the given inital values.
     * @param repoName The repo from which the results where collected.
     * @param totalCommits The total number of commits in the observed history of the given repository.
     * @param exportedCommits The number of commits that were processed. exportedCommits &lt;= totalCommits
     * @param emptyCommits Number of commits that were not processed because they had no DiffTrees.
     *                     A commit is empty iff at least of one of the following conditions is met for every of its patches:
     *                       - the patch did not edit a C file,
     *                       - the DiffTree became empty after transformations (this can happen if there are only whitespace changes),
     *                       - or the patch had syntax errors in its annotations, so the DiffTree could not be parsed.
     * @param failedCommits Number of commits that could not be parsed at all because of exceptions when operating JGit.
     *                      The number of commits that were filtered because they are a merge commit is thus given as
     *                      totalCommits - exportedCommits - emptyCommits - failedCommits
     * @param exportedTrees Number of DiffTrees that were processed.
     * @param runtimeInSeconds The total runtime in seconds (irrespective of multithreading).
     * @param runtimeWithMultithreadingInSeconds The effective runtime in seconds that we have when using multithreading.
     * @param min The commit that was processed the fastest.
     * @param max The commit that was processed the slowest.
     * @param debugData Debug data for DiffTree serialization.
     */
    public AnalysisResult(
            final String repoName,
            int totalCommits,
            int exportedCommits,
            int emptyCommits,
            int failedCommits,
            int exportedTrees,
            double runtimeInSeconds,
            double runtimeWithMultithreadingInSeconds,
            final CommitProcessTime min,
            final CommitProcessTime max,
            final DiffTreeSerializeDebugData debugData)
    {
        this.repoName = repoName;
        this.totalCommits = totalCommits;
        this.exportedCommits = exportedCommits;
        this.emptyCommits = emptyCommits;
        this.failedCommits = failedCommits;
        this.exportedTrees = exportedTrees;
        this.runtimeInSeconds = runtimeInSeconds;
        this.runtimeWithMultithreadingInSeconds = runtimeWithMultithreadingInSeconds;
        this.debugData = debugData;
        this.min = min;
        this.max = max;
    }

    /**
     * Stores the given custom key value information in this analysis result.
     * @param key The name of the given value that is used to associate the value.
     * @param value The value to store.
     */
    public void putCustomInfo(final String key, final String value) {
        customInfo.put(key, value);
    }

    /**
     * Report errors (that for example occurred when parsing DiffTrees).
     * @param errors A list of errors to report.
     */
    public void reportDiffErrors(final List<DiffError> errors) {
        for (final DiffError e : errors) {
            diffErrors.put(e, 1);
        }
    }

    @Override
    public LinkedHashMap<String, Object> snapshot() {
        LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
        snap.put(MetadataKeys.REPONAME, repoName);
        snap.put(MetadataKeys.TOTAL_COMMITS, totalCommits);
        snap.put(MetadataKeys.FILTERED_COMMITS, totalCommits - exportedCommits - emptyCommits - failedCommits);
        snap.put(MetadataKeys.FAILED_COMMITS, failedCommits);
        snap.put(MetadataKeys.EMPTY_COMMITS, emptyCommits);
        snap.put(MetadataKeys.PROCESSED_COMMITS, exportedCommits);
        snap.put(MetadataKeys.TREES, exportedTrees);
        snap.put(MetadataKeys.MINCOMMIT, min.toString());
        snap.put(MetadataKeys.MAXCOMMIT, max.toString());
        snap.put(MetadataKeys.RUNTIME, runtimeInSeconds);
        snap.put(MetadataKeys.RUNTIME_WITH_MULTITHREADING, runtimeWithMultithreadingInSeconds);
        snap.putAll(customInfo);
        snap.putAll(debugData.snapshot());
        snap.putAll(Functjonal.bimap(diffErrors, error -> ERROR_BEGIN + error + ERROR_END, Object::toString));
        return snap;
    }
}

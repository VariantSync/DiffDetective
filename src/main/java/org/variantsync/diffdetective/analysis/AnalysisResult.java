package org.variantsync.diffdetective.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.functjonal.Cast;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.category.InplaceMonoid;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.map.MergeMap;

/**
 * The result of a {@link Analysis}.
 * This result stores various metadata and statistics that we use for the validation of our ESEC/FSE paper.
 * An AnalysisResult also allows to store any custom metadata or information.
 * @author Paul Bittner
 */
public final class AnalysisResult implements Metadata<AnalysisResult> {
    /**
     * Placeholder name for data that is not associated to a repository or where the repository is unknown.
     */
    public final static String NO_REPO = "<NONE>";

    private final static String ERROR_BEGIN = "#Error[";
    private final static String ERROR_END = "]";

    /**
     * The repo from which the results where collected.
     */
    public String repoName = NO_REPO;
    public String taskName;
    /**
     * The effective runtime in seconds that we have when using multithreading.
     */
    public double runtimeWithMultithreadingInSeconds = 0;
    /**
     * The total number of commits in the observed history of the given repository.
     */
    public int totalCommits = 0;
    public final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    private final Map<String, Metadata<?>> results = new HashMap<>();

    /**
     * Type proxy and runtime key for the type of a {@code Metadata} subclass.
     * There should be no two {@code ResultKey} instances with the same {@code key} but different
     * types {@code T}, otherwise {@link get} or {@link append} may throw {@link
     * ClassCastException}s.
     *
     * @param key the runtime key for looking up the requested type
     * @param <T> a subclass of {@code Metadata}
     */
    public record ResultKey<T extends Metadata<T>>(String key) {
    }

    /**
     * Returns the value previously added using {@link append}.
     *
     * @param resultKey the key which is used to identify the data and its type
     * @param <T> the type of the value which was previously stored
     */
    public <T extends Metadata<T>> T get(ResultKey<T> resultKey) {
        return Cast.unchecked(results.get(resultKey.key()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void unsafeAppend(String key, Metadata<?> value) {
        results.merge(key, value, (first, second) -> {
            // `first` and `second` should have the same type if there are no two
            // `ResultKey` instances with the same `ResultKey.key` and `results` is only
            // modified by `append`.
            ((Metadata) first).append((Metadata) second);
            return first;
        });
    }

    /**
     * Adds a new value or {@link Metadata#append}s it to the old value which is indexed by {@code
     * resultKey}.
     *
     * @param resultKey the key which is used to identify the data and its type
     * @param <T> the type of the value which is appended
     * @see get
     */
    public <T extends Metadata<T>> void append(ResultKey<T> resultKey, T value) {
        unsafeAppend(resultKey.key(), value);
    }

    /**
     * Inplace semigroup for AnalysisResult.
     * Merges the second results values into the first result.
     */
    public static final InplaceSemigroup<AnalysisResult> ISEMIGROUP = (a, b) -> {
        a.repoName = Metadata.mergeIfEqualElse(a.repoName, b.repoName,
                (ar, br) -> {
                    Logger.warn("Merging analysis for different repos {} and {}!", ar, br);
                    return ar + "; " + br;
                });
        a.taskName = Metadata.mergeEqual(a.taskName, b.taskName);
        a.runtimeWithMultithreadingInSeconds += b.runtimeWithMultithreadingInSeconds;
        a.totalCommits += b.totalCommits;
        a.diffErrors.append(b.diffErrors);
        b.results.forEach((key, value) -> a.unsafeAppend(key, value));
    };

    public static final InplaceMonoid<AnalysisResult> IMONOID =
        InplaceMonoid.From(AnalysisResult::new, ISEMIGROUP);

    @Override
    public InplaceSemigroup<AnalysisResult> semigroup() {
        return ISEMIGROUP;
    }

    public AnalysisResult() {
        this(NO_REPO);
    }

    /**
     * Creates an empty analysis result for the given repo.
     * @param repoName The repo for which to collect results.
     */
    public AnalysisResult(final String repoName) {
        this.repoName = repoName;
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
        snap.put(MetadataKeys.TASKNAME, taskName);
        snap.put(MetadataKeys.RUNTIME_WITH_MULTITHREADING, runtimeWithMultithreadingInSeconds);
        snap.put(MetadataKeys.TOTAL_COMMITS, totalCommits);

        var statistics = get(StatisticsAnalysis.RESULT);
        if (statistics != null) {
            snap.put(MetadataKeys.FILTERED_COMMITS, totalCommits - statistics.processedCommits - statistics.emptyCommits - statistics.failedCommits);
        }

        snap.putAll(Functjonal.bimap(diffErrors, error -> ERROR_BEGIN + error + ERROR_END, Object::toString));
        snap.put(MetadataKeys.REPONAME, repoName);
        for (var result : results.values()) {
            snap.putAll(result.snapshot());
        }
        return snap;
    }

    @Override
    public void setFromSnapshot(LinkedHashMap<String, String> snap) {
        repoName = snap.get(MetadataKeys.REPONAME);
        taskName = snap.get(MetadataKeys.TASKNAME);

        String runtime = snap.get(MetadataKeys.RUNTIME_WITH_MULTITHREADING);
        if (runtime.endsWith("s")) {
            runtime = runtime.substring(0, runtime.length() - 1);
        }
        runtimeWithMultithreadingInSeconds = Double.parseDouble(runtime);

        totalCommits = Integer.parseInt(snap.get(MetadataKeys.TOTAL_COMMITS));

        for (var entry : snap.entrySet()) {
            String key = entry.getKey();
            if (entry.getKey().startsWith(ERROR_BEGIN)) {
                var errorId = key.substring(ERROR_BEGIN.length(), key.length() - ERROR_END.length());
                var e = DiffError.fromMessage(errorId);
                if (e.isEmpty()) {
                    throw new RuntimeException("Invalid error id " + errorId);
                }
                // add DiffError
                diffErrors.put(e.get(), Integer.parseInt(entry.getValue()));
            }
        }

        for (final Metadata<?> result : results.values()) {
            result.setFromSnapshot(snap);
        }
    }

    public void setFrom(final Path path) throws IOException {
        var snapshot = new LinkedHashMap<String, String>();

        try (BufferedReader input = Files.newBufferedReader(path)) {
            // examine each line of the metadata file separately
            String line;
            while ((line = input.readLine()) != null) {
                String[] keyValuePair = line.split(": ");
                snapshot.put(keyValuePair[0], keyValuePair[1]);
            }
        }

        setFromSnapshot(snapshot);
    }
}

package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.metadata.ElementaryPatternCount;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.functjonal.Functjonal;
import org.variantsync.functjonal.category.InplaceMonoid;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.category.Semigroup;
import org.variantsync.functjonal.map.MergeMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class FeatureSplitResult implements Metadata<FeatureSplitResult> {
    public final static String NO_REPO = "<NONE>";
    public final static String EXTENSION = ".metadata.txt";
    public final static String ERROR_BEGIN = "#Error[";
    public final static String ERROR_END = "]";

    public static Map.Entry<String, BiConsumer<FeatureSplitResult, String>> storeAsCustomInfo(String key) {
        return Map.entry(key, (r, val) -> r.putCustomInfo(key, val));
    }

    /**
     * The stored function adds all results form object b to object a
     */
    public final static InplaceSemigroup<FeatureSplitResult> ISEMIGROUP = (a, b) -> {
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
        a.filterHits.append(b.filterHits);
        a.elementaryPatternCounts.append(b.elementaryPatternCounts);
        MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
        a.diffErrors.append(b.diffErrors);
    };

    /**
     * Used in initialization, which requires an empty semigroup
     */
    public static InplaceMonoid<FeatureSplitResult> IMONOID= InplaceMonoid.From(
            FeatureSplitResult::new,
            ISEMIGROUP
    );

    public String repoName;
    public int totalCommits; // total number of commits in the observed history
    public int exportedCommits; // number of processed commits in the observed history. exportedCommits <= totalCommits
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
    public int exportedTrees; // number of extracted DiffTrees in the observed history.
    public double runtimeInSeconds;
    public double runtimeWithMultithreadingInSeconds;
    public final CommitProcessTime min, max;
    public final DiffTreeSerializeDebugData debugData;

    // FeatureSplit specific information
    public ExplainedFilterSummary filterHits;
    public ElementaryPatternCount elementaryPatternCounts;
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    private final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    public FeatureSplitResult() {
        this(NO_REPO);
    }

    public FeatureSplitResult(final String repoName) {
        this(
                repoName,
                0, 0, 0, 0,
                0,
                0, 0,
                CommitProcessTime.Unknown(repoName, Long.MAX_VALUE),
                CommitProcessTime.Unknown(repoName, Long.MIN_VALUE),
                new DiffTreeSerializeDebugData(),
                new ExplainedFilterSummary());
    }

    public FeatureSplitResult(
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
            final DiffTreeSerializeDebugData debugData,
            final ExplainedFilterSummary filterHits)
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
        this.filterHits = filterHits;
        this.elementaryPatternCounts = new ElementaryPatternCount();
        this.min = min;
        this.max = max;
    }

    public void putCustomInfo(final String key, final String value) {
        customInfo.put(key, value);
    }

    public void reportDiffErrors(final List<DiffError> errors) {
        for (final DiffError e : errors) {
            diffErrors.put(e, 1);
        }
    }

    /**
     * Creates a key-value store of metadata generated FeatureSplit
     * @return A LinkedHashMap that stores all relevant properties to export.
     */
    @Override
    public LinkedHashMap<String, Object> snapshot() {
        LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
        // TODO change objects in snapshot
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
        snap.putAll(filterHits.snapshot());
        snap.putAll(elementaryPatternCounts.snapshot());
        snap.putAll(Functjonal.bimap(diffErrors, error -> ERROR_BEGIN + error + ERROR_END, Object::toString));
        return snap;
    }

    @Override
    public InplaceSemigroup<FeatureSplitResult> semigroup() {
        return ISEMIGROUP;
    }
}

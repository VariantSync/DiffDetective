package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.variation.diff.serialize.DiffTreeSerializeDebugData;
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

/**
 * The result of a {@link Analysis}.
 * This result stores various metadata and statistics that we use for the validation of our ESEC/FSE paper.
 * An AnalysisResult also allows to store any custom metadata or information.
 * @author Paul Bittner
 */
public class AnalysisResult implements Metadata<AnalysisResult> {
    /**
     * Placeholder name for data that is not associated to a repository or where the repository is unknown.
     */
    public final static String NO_REPO = "<NONE>";

    /**
     * File extension that is used when writing AnalysisResults to disk.
     */
    public final static String EXTENSION = ".metadata.txt";

    private final static String ERROR_BEGIN = "#Error[";
    private final static String ERROR_END = "]";

    /**
     * Inplace semigroup for AnalysisResult.
     * Merges the second results values into the first result.
     */
    public final static InplaceSemigroup<AnalysisResult> ISEMIGROUP = (a, b) -> {
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
        a.editClassCounts.append(b.editClassCounts);
        MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
        a.diffErrors.append(b.diffErrors);
    };

    /**
     * Inplace monoid for AnalysisResult.
     * @see AnalysisResult#ISEMIGROUP
     */
    public static InplaceMonoid<AnalysisResult> IMONOID= InplaceMonoid.From(
            AnalysisResult::new,
            ISEMIGROUP
    );

    /**
     * The repo from which the results where collected.
     */
    public String repoName = NO_REPO;
    /**
     * The total number of commits in the observed history of the given repository.
     */
    public int totalCommits = 0;
    /**
     * The number of commits that were processed.
     * {@code exportedCommits <= totalCommits}
     */
    public int exportedCommits = 0;
    /**
     * Number of commits that were not processed because they had no DiffTrees.
     * A commit is empty iff at least of one of the following conditions is met for every of its patches:
     * <ul>
     * <li>the patch did not edit a C file,
     * <li>the DiffTree became empty after transformations (this can happen if there are only whitespace changes),
     * <li>or the patch had syntax errors in its annotations, so the DiffTree could not be parsed.
     * </ul>
     */
    public int emptyCommits = 0;
    /**
     * Number of commits that could not be parsed at all because of exceptions when operating JGit.
     *
     * The number of commits that were filtered because they are a merge commit is thus given as
     * {@code totalCommits - exportedCommits - emptyCommits - failedCommits}
     */
    public int failedCommits = 0;
    /**
     * Number of DiffTrees that were processed.
     */
    public int exportedTrees = 0;
    /**
     * The total runtime in seconds (irrespective of multithreading).
     */
    public double runtimeInSeconds = 0;
    /**
     * The effective runtime in seconds that we have when using multithreading.
     */
    public double runtimeWithMultithreadingInSeconds = 0;
    /**
     * The commit that was processed the fastest.
     */
    public final CommitProcessTime min;
    /**
     * The commit that was processed the slowest.
     */
    public final CommitProcessTime max;
    /**
     * Debug data for DiffTree serialization.
     */
    public final DiffTreeSerializeDebugData debugData = new DiffTreeSerializeDebugData();
    /**
     * Explanations for filter hits, when filtering DiffTrees (e.g., because a diff was empty).
     */
    public ExplainedFilterSummary filterHits = new ExplainedFilterSummary();
    public EditClassCount editClassCounts = new EditClassCount();
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    private final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    public AnalysisResult() {
        this(NO_REPO);
    }

    /**
     * Creates an empty analysis result for the given repo.
     * @param repoName The repo for which to collect results.
     */
    public AnalysisResult(final String repoName) {
        this.repoName = repoName;

        this.min = CommitProcessTime.Unknown(repoName, Long.MAX_VALUE);
        this.max = CommitProcessTime.Unknown(repoName, Long.MIN_VALUE);
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
    
    /**
     * Imports a metadata file, which is an output of a {@link AnalysisResult}, and saves back to {@link AnalysisResult}.
     * 
     * @param p {@link Path} to the metadata file
     * @param customParsers A list of parsers to handle custom values that were stored with {@link AnalysisResult#putCustomInfo(String, String)}.
     *                      Each parser parses the value (second argument) of a given key (first entry in the map) and stores it in the given AnalysisResult (first argument).
     * @return The reconstructed {@link AnalysisResult}
     * @throws IOException when the file could not be read.
     */
    public static AnalysisResult importFrom(final Path p, final Map<String, BiConsumer<AnalysisResult, String>> customParsers) throws IOException {
        AnalysisResult result = new AnalysisResult();
        
        final List<String> filterHitsLines = new ArrayList<>();
        final List<String> editClassCountsLines = new ArrayList<>();

        try (BufferedReader input = Files.newBufferedReader(p)) {
            // examine each line of the metadata file separately
            String line;
            while ((line = input.readLine()) != null) {
                String[] keyValuePair = line.split(": ");
                String key = keyValuePair[0];
                String value = keyValuePair[1];

                switch (key) {
                    case MetadataKeys.REPONAME -> result.repoName = value;
                    case MetadataKeys.TREES -> result.exportedTrees = Integer.parseInt(value);
                    case MetadataKeys.PROCESSED_COMMITS -> result.exportedCommits = Integer.parseInt(value);
                    case MetadataKeys.TOTAL_COMMITS -> result.totalCommits = Integer.parseInt(value);
                    case MetadataKeys.EMPTY_COMMITS -> result.emptyCommits = Integer.parseInt(value);
                    case MetadataKeys.FAILED_COMMITS -> result.failedCommits = Integer.parseInt(value);
                    case MetadataKeys.FILTERED_COMMITS -> { /* Do nothing because this value is derived. */ }
                    case MetadataKeys.NON_NODE_COUNT -> result.debugData.numExportedNonNodes = Integer.parseInt(value);
                    case MetadataKeys.ADD_NODE_COUNT -> result.debugData.numExportedAddNodes = Integer.parseInt(value);
                    case MetadataKeys.REM_NODE_COUNT -> result.debugData.numExportedRemNodes = Integer.parseInt(value);
                    case MetadataKeys.MINCOMMIT -> result.min.set(CommitProcessTime.fromString(value));
                    case MetadataKeys.MAXCOMMIT -> result.max.set(CommitProcessTime.fromString(value));
                    case MetadataKeys.RUNTIME -> {
                        if (value.endsWith("s")) {
                            value = value.substring(0, value.length() - 1);
                        }
                        result.runtimeInSeconds = Double.parseDouble(value);
                    }
                    case MetadataKeys.RUNTIME_WITH_MULTITHREADING -> {
                        if (value.endsWith("s")) {
                            value = value.substring(0, value.length() - 1);
                        }
                        result.runtimeWithMultithreadingInSeconds = Double.parseDouble(value);
                    }
                    default -> {

                        // temporary fix for renaming from Unchanged to Untouched
                        final String unchanged = "Unchanged";
                        if (key.startsWith(unchanged)) {
                            key = ProposedEditClasses.Untouched.getName();
                            line = key + line.substring(unchanged.length());
                        }

                        final String finalKey = key;
                        if (ProposedEditClasses.All.stream().anyMatch(editClass -> editClass.getName().equals(finalKey))) {
                            editClassCountsLines.add(line);
                        } else if (key.startsWith(ExplainedFilterSummary.FILTERED_MESSAGE_BEGIN)) {
                            filterHitsLines.add(line);
                        } else if (key.startsWith(ERROR_BEGIN)) {
                            var errorId = key.substring(ERROR_BEGIN.length(), key.length() - ERROR_END.length());
                            var e = DiffError.fromMessage(errorId);
                            if (e.isEmpty()) {
                                throw new RuntimeException("Invalid error id " + errorId + " while importing " + p);
                            }
                            // add DiffError
                            result.diffErrors.put(e.get(), Integer.parseInt(value));
                        } else {
                            final BiConsumer<AnalysisResult, String> customParser = customParsers.get(key);
                            if (customParser == null) {
                                final String errorMessage = "Unknown entry \"" + line + "\"!";
                                throw new IOException(errorMessage);
                            } else {
                                customParser.accept(result, value);
                            }
                        }
                    }
                }
            }
        }

        result.filterHits = ExplainedFilterSummary.parse(filterHitsLines);
        result.editClassCounts = EditClassCount.parse(editClassCountsLines, p.toString());

        return result;
    }

    /**
     * Helper method to construct custom parsers for {@link AnalysisResult#importFrom(Path, Map)}.
     * This method creates a parser for custom values that just stores the parsed values as string values for the given key.
     * @param key The key whose values should be stored as unparsed strings.
     * @return A custom parser for {@link AnalysisResult#importFrom(Path, Map)}.
     */
    public static Map.Entry<String, BiConsumer<AnalysisResult, String>> storeAsCustomInfo(String key) {
        return Map.entry(key, (r, val) -> r.putCustomInfo(key, val));
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
        snap.putAll(filterHits.snapshot());
        snap.putAll(editClassCounts.snapshot());
        snap.putAll(Functjonal.bimap(diffErrors, error -> ERROR_BEGIN + error + ERROR_END, Object::toString));
        return snap;
    }

    @Override
    public InplaceSemigroup<AnalysisResult> semigroup() {
        return ISEMIGROUP;
    }
}

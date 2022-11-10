package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
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
 * The result of a {@link HistoryAnalysis}.
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
        a.implicationEdges += b.implicationEdges;
        a.alternativeEdges += b.alternativeEdges;
        a.runtimeInSeconds += b.runtimeInSeconds;
        a.runtimeWithMultithreadingInSeconds += b.runtimeWithMultithreadingInSeconds;
        a.edgeAddingRuntimeInMilliseconds += b.edgeAddingRuntimeInMilliseconds;
        a.min.set(CommitProcessTime.min(a.min, b.min));
        a.max.set(CommitProcessTime.max(a.max, b.max));
        a.debugData.append(b.debugData);
        a.filterHits.append(b.filterHits);
        a.editClassCounts.append(b.editClassCounts);
        MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
        a.diffErrors.append(b.diffErrors);
        a.complexityChangeCount[0] += b.complexityChangeCount[0];
        a.complexityChangeCount[1] += b.complexityChangeCount[1];
        a.complexityChangeCount[2] += b.complexityChangeCount[2];
        a.complexityChangeCount[3] += b.complexityChangeCount[3];
        a.complexityChangeCount[4] += b.complexityChangeCount[4];
        a.complexityChangeCount[5] += b.complexityChangeCount[5];
        a.complexityChangeCount[6] += b.complexityChangeCount[6];
        a.falseNodes += b.falseNodes;
    };

    /**
     * Inplace monoid for AnalysisResult.
     * @see AnalysisResult#ISEMIGROUP
     */
    public static InplaceMonoid<AnalysisResult> IMONOID= InplaceMonoid.From(
            AnalysisResult::new,
            ISEMIGROUP
    );

    public String repoName;
    public int totalCommits;
    public int exportedCommits;
    public int emptyCommits;
    public int failedCommits;
    public int exportedTrees;
    public int implicationEdges;
    public int alternativeEdges;
    public int falseNodes;
    public double runtimeInSeconds;
    public double runtimeWithMultithreadingInSeconds;
    public double edgeAddingRuntimeInMilliseconds;
    public final CommitProcessTime min, max;
    public final DiffTreeSerializeDebugData debugData;
    public ExplainedFilterSummary filterHits;
    public int[] complexityChangeCount;
    public EditClassCount editClassCounts;
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    private final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

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
                new DiffTreeSerializeDebugData(),
                new ExplainedFilterSummary());
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
     * @param filterHits Explanations for filter hits, when filtering DiffTrees (e.g., because a diff was empty).
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
        this.editClassCounts = new EditClassCount();
        this.min = min;
        this.max = max;
        this.implicationEdges = 0;
        this.alternativeEdges = 0;
        this.complexityChangeCount = new int[7];
        this.falseNodes = 0;
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
                    case MetadataKeys.IMPLICATION_EDGES -> result.implicationEdges = Integer.parseInt(value);
                    case MetadataKeys.ALTERNATIVE_EDGES -> result.alternativeEdges = Integer.parseInt(value);
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
        snap.put(MetadataKeys.IMPLICATION_EDGES, implicationEdges);
        snap.put(MetadataKeys.ALTERNATIVE_EDGES, alternativeEdges);
        snap.put("complexity unchanged", complexityChangeCount[0]);
        snap.put("complexity +<5 %", complexityChangeCount[1]);
        snap.put("complexity +5-10 %", complexityChangeCount[2]);
        snap.put("complexity +10-20 %", complexityChangeCount[3]);
        snap.put("complexity +20-40 %", complexityChangeCount[4]);
        snap.put("complexity +40-60 %", complexityChangeCount[5]);
        snap.put("complexity +>60 %", complexityChangeCount[6]);
        snap.put(MetadataKeys.MINCOMMIT, min.toString());
        snap.put(MetadataKeys.MAXCOMMIT, max.toString());
        snap.put(MetadataKeys.RUNTIME, runtimeInSeconds);
        snap.put(MetadataKeys.RUNTIME_WITH_MULTITHREADING, runtimeWithMultithreadingInSeconds);
        snap.put(MetadataKeys.EDGE_ADDING_TIME, edgeAddingRuntimeInMilliseconds);
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

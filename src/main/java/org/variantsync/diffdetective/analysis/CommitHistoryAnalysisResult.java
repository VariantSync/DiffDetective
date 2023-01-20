package org.variantsync.diffdetective.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.functjonal.category.InplaceMonoid;
import org.variantsync.functjonal.category.InplaceSemigroup;

public class CommitHistoryAnalysisResult extends AnalysisResult<CommitHistoryAnalysisResult> {
    public static InplaceSemigroup<CommitHistoryAnalysisResult> ISEMIGROUP =
        (a, b) -> AnalysisResult.<CommitHistoryAnalysisResult>ISEMIGROUP().append(a, b);

    /**
     * Inplace monoid for CommitHistoryAnalysisResult.
     * @see CommitHistoryAnalysisResult#ISEMIGROUP
     */
    public static InplaceMonoid<CommitHistoryAnalysisResult> IMONOID = InplaceMonoid.From(
            CommitHistoryAnalysisResult::new,
            ISEMIGROUP
    );

    @Override
    public InplaceSemigroup<CommitHistoryAnalysisResult> semigroup() {
        return ISEMIGROUP;
    }

    public ExplainedFilterSummary filterHits;
    public EditClassCount editClassCounts;

    public CommitHistoryAnalysisResult() {
        this(NO_REPO);
    }

    public CommitHistoryAnalysisResult(final String repoName) {
        this(
            repoName,
            new ExplainedFilterSummary()
        );
    }

    public CommitHistoryAnalysisResult(
        final String repoName,
        final ExplainedFilterSummary filterHits
    ) {
        super(repoName);
        this.filterHits = filterHits;
        this.editClassCounts = new EditClassCount();
    }

    @Override
    public LinkedHashMap<String, Object> snapshot() {
        LinkedHashMap<String, Object> snap = super.snapshot();

        snap.putAll(filterHits.snapshot());
        snap.putAll(editClassCounts.snapshot());

        return snap;
    }

    /**
     * Imports a metadata file, which is an output of a {@link CommitHistoryAnalysisResult}, and saves back to {@link CommitHistoryAnalysisResult}.
     *
     * @param p {@link Path} to the metadata file
     * @param customParsers A list of parsers to handle custom values that were stored with {@link CommitHistoryAnalysisResult#putCustomInfo(String, String)}.
     *                      Each parser parses the value (second argument) of a given key (first entry in the map) and stores it in the given CommitHistoryAnalysisResult (first argument).
     * @return The reconstructed {@link CommitHistoryAnalysisResult}
     * @throws IOException when the file could not be read.
     */
    public static CommitHistoryAnalysisResult importFrom(final Path p, final Map<String, BiConsumer<CommitHistoryAnalysisResult, String>> customParsers) throws IOException {
        var result = new CommitHistoryAnalysisResult();

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
                            DiffError e = new DiffError(key.substring(ERROR_BEGIN.length(), key.length() - ERROR_END.length()));
                            // add DiffError
                            result.diffErrors.put(e, Integer.parseInt(value));
                        } else {
                            final BiConsumer<CommitHistoryAnalysisResult, String> customParser = customParsers.get(key);
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
     * Helper method to construct custom parsers for {@link CommitHistoryAnalysisResult#importFrom(Path, Map)}.
     * This method creates a parser for custom values that just stores the parsed values as string values for the given key.
     * @param key The key whose values should be stored as unparsed strings.
     * @return A custom parser for {@link CommitHistoryAnalysisResult#importFrom(Path, Map)}.
     */
    public static Map.Entry<String, BiConsumer<CommitHistoryAnalysisResult, String>> storeAsCustomInfo(String key) {
        return Map.entry(key, (r, val) -> r.putCustomInfo(key, val));
    }
}

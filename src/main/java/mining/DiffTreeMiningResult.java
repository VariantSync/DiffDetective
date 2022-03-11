package mining;

import de.variantsync.functjonal.Functjonal;
import de.variantsync.functjonal.category.InplaceMonoid;
import de.variantsync.functjonal.category.InplaceSemigroup;
import de.variantsync.functjonal.category.Semigroup;
import de.variantsync.functjonal.map.MergeMap;
import diff.difftree.serialize.DiffTreeSerializeDebugData;
import diff.result.DiffError;
import metadata.AtomicPatternCount;
import metadata.ExplainedFilterSummary;
import metadata.Metadata;
import mining.strategies.CommitProcessTime;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class DiffTreeMiningResult implements Metadata<DiffTreeMiningResult> {
    public final static String NO_REPO = "<NONE>";
    public final static String EXTENSION = ".metadata.txt";
    public final static String ERROR_BEGIN = "#Error[";
    public final static String ERROR_END = "]";

    public static Map.Entry<String, BiConsumer<DiffTreeMiningResult, String>> storeAsCustomInfo(String key) {
        return Map.entry(key, (r, val) -> r.putCustomInfo(key, val));
    }
    
    public final static InplaceSemigroup<DiffTreeMiningResult> ISEMIGROUP = (a, b) -> {
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
        a.atomicPatternCounts.append(b.atomicPatternCounts);
        MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
        a.diffErrors.append(b.diffErrors);
    };

    public static InplaceMonoid<DiffTreeMiningResult> IMONOID= InplaceMonoid.From(
            DiffTreeMiningResult::new,
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
    public int exportedTrees;
    public double runtimeInSeconds;
    public double runtimeWithMultithreadingInSeconds;
    public final CommitProcessTime min, max;
    public final DiffTreeSerializeDebugData debugData;
    public ExplainedFilterSummary filterHits;
    public AtomicPatternCount atomicPatternCounts;
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    private final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    public DiffTreeMiningResult() {
        this(NO_REPO);
    }

    public DiffTreeMiningResult(final String repoName) {
        this(repoName, 0, 0, 0, 0, 0, 0, 0, CommitProcessTime.Unknown(repoName, Long.MAX_VALUE), CommitProcessTime.Unknown(repoName, Long.MIN_VALUE), new DiffTreeSerializeDebugData(), new ExplainedFilterSummary());
    }

    public DiffTreeMiningResult(
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
        this.atomicPatternCounts = new AtomicPatternCount();
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
     * Imports a metadata file, which is an output of a {@link DiffTreeMiningResult}, and saves back to {@link DiffTreeMiningResult}.
     * 
     * @param p {@link Path} to the metadata file
     * @return The reconstructed {@link DiffTreeMiningResult}
     * @throws IOException
     */
    public static DiffTreeMiningResult importFrom(final Path p, final Map<String, BiConsumer<DiffTreeMiningResult, String>> customParsers) throws IOException {
        DiffTreeMiningResult result = new DiffTreeMiningResult();
        
        final List<String> filterHitsLines = new ArrayList<>();
        final List<String> atomicPatternCountsLines = new ArrayList<>();
        
        String fileInput = IO.readAsString(p); // read in metadata file
        fileInput = fileInput.replace("\r", ""); // remove carriage returns if present
        final String[] lines = fileInput.split("\n");
        String[] keyValuePair;
        String key;
        String value;
        
        // examine each line of the metadata file separately
        for (/*final*/ String line : lines) {
            keyValuePair = line.split(": ");
            key = keyValuePair[0];
            value = keyValuePair[1];

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
                        key = ProposedAtomicPatterns.Untouched.getName();
                        line = key + line.substring(unchanged.length());
                    }

                    final String finalKey = key;
                    if (ProposedAtomicPatterns.All.stream().anyMatch(pattern -> pattern.getName().equals(finalKey))) {
                        atomicPatternCountsLines.add(line);
                    } else if (key.startsWith(ExplainedFilterSummary.FILTERED_MESSAGE_BEGIN)) {
                        filterHitsLines.add(line);
                    } else if (key.startsWith(ERROR_BEGIN)) {
                        DiffError e = new DiffError(key.substring(ERROR_BEGIN.length(), key.length() - ERROR_END.length()));
                        // add DiffError
                        result.diffErrors.put(e, Integer.parseInt(value));
                    } else {
                        final BiConsumer<DiffTreeMiningResult, String> customParser = customParsers.get(key);
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
        
        result.filterHits = ExplainedFilterSummary.parse(filterHitsLines);
        result.atomicPatternCounts = AtomicPatternCount.parse(atomicPatternCountsLines, p.toString());

        return result;
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
        snap.putAll(atomicPatternCounts.snapshot());
        snap.putAll(Functjonal.bimap(diffErrors, error -> ERROR_BEGIN + error + ERROR_END, Object::toString));
        return snap;
    }

    @Override
    public InplaceSemigroup<DiffTreeMiningResult> semigroup() {
        return ISEMIGROUP;
    }
}

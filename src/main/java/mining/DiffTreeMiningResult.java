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
    public final static String EXTENSION = ".metadata.txt";
    public final static String ERROR_BEGIN = "#Error[";
    public final static String ERROR_END = "]";

    public static Map.Entry<String, BiConsumer<DiffTreeMiningResult, String>> storeAsCustomInfo(String key) {
        return Map.entry(key, (r, val) -> r.putCustomInfo(key, val));
    }
    
    public final static InplaceSemigroup<DiffTreeMiningResult> ISEMIGROUP = (a, b) -> {
        a.exportedCommits += b.exportedCommits;
        a.exportedTrees += b.exportedTrees;
        a.runtimeInSeconds += b.runtimeInSeconds;
        a.min.set(CommitProcessTime.min(a.min, b.min));
        a.max.set(CommitProcessTime.max(a.max, b.max));
        a.debugData.append(b.debugData);
        a.filterHits.append(b.filterHits);
        a.atomicPatternCounts.append(b.atomicPatternCounts);
        MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
        a.diffErrors.append(b.diffErrors);
    };

    public final static InplaceMonoid<DiffTreeMiningResult> IMONOID = InplaceMonoid.From(
            DiffTreeMiningResult::new,
            ISEMIGROUP
    );

    public int exportedCommits;
    public int exportedTrees;
    public double runtimeInSeconds;
    public final CommitProcessTime min, max;
    public final DiffTreeSerializeDebugData debugData;
    public ExplainedFilterSummary filterHits;
    public AtomicPatternCount atomicPatternCounts;
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    private final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    public DiffTreeMiningResult() {
        this(0, 0, 0, CommitProcessTime.Unknown(Double.MAX_VALUE), CommitProcessTime.Unknown(Double.MIN_VALUE), new DiffTreeSerializeDebugData(), new ExplainedFilterSummary());
    }

    public DiffTreeMiningResult(
            int exportedCommits,
            int exportedTrees,
            double runtimeInSeconds,
            final CommitProcessTime min,
            final CommitProcessTime max,
            final DiffTreeSerializeDebugData debugData,
            final ExplainedFilterSummary filterHits)
    {
        this.exportedCommits = exportedCommits;
        this.exportedTrees = exportedTrees;
        this.runtimeInSeconds = runtimeInSeconds;
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
        for (final String line : lines) {
            keyValuePair = line.split(": ");
            key = keyValuePair[0];
            value = keyValuePair[1];

            switch (key) {
                case MetadataKeys.TREES -> result.exportedTrees = Integer.parseInt(value);
                case MetadataKeys.COMMITS -> result.exportedCommits = Integer.parseInt(value);
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
                default -> {
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
        snap.put(MetadataKeys.TREES, exportedTrees);
        snap.put(MetadataKeys.COMMITS, exportedCommits);
        snap.put(MetadataKeys.RUNTIME, runtimeInSeconds);
        snap.put(MetadataKeys.MINCOMMIT, min.toString());
        snap.put(MetadataKeys.MAXCOMMIT, max.toString());
        snap.putAll(debugData.snapshot());
        snap.putAll(filterHits.snapshot());
        snap.putAll(atomicPatternCounts.snapshot());
        snap.putAll(customInfo);
        snap.putAll(Functjonal.bimap(diffErrors, error -> ERROR_BEGIN + error + ERROR_END, Object::toString));
        return snap;
    }

    @Override
    public InplaceSemigroup<DiffTreeMiningResult> semigroup() {
        return ISEMIGROUP;
    }
}

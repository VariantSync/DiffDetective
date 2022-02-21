package mining;

import de.variantsync.functjonal.Functjonal;
import de.variantsync.functjonal.category.InplaceSemigroup;
import de.variantsync.functjonal.category.Semigroup;
import de.variantsync.functjonal.map.MergeMap;
import diff.difftree.serialize.DiffTreeSerializeDebugData;
import diff.result.DiffError;
import metadata.AtomicPatternCount;
import metadata.ExplainedFilterSummary;
import metadata.Metadata;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DiffTreeMiningResult implements Metadata<DiffTreeMiningResult> {
    public final static String EXTENSION = ".metadata.txt";
    public final static String ERROR_BEGIN = "#Error[";
    public final static String ERROR_END = "]";
    
    public final static InplaceSemigroup<DiffTreeMiningResult> ISEMIGROUP = (a, b) -> {
        a.exportedCommits += b.exportedCommits;
        a.exportedTrees += b.exportedTrees;
        a.debugData.append(b.debugData);
        a.filterHits.append(b.filterHits);
        a.atomicPatternCounts.append(b.atomicPatternCounts);
        MergeMap.putAllValues(a.customInfo, b.customInfo, Semigroup.assertEquals());
        a.diffErrors.append(b.diffErrors);
    };

    public int exportedCommits;
    public int exportedTrees;
    public final DiffTreeSerializeDebugData debugData;
    public ExplainedFilterSummary filterHits;
    public AtomicPatternCount atomicPatternCounts;
    private final LinkedHashMap<String, String> customInfo = new LinkedHashMap<>();
    private final MergeMap<DiffError, Integer> diffErrors = new MergeMap<>(new HashMap<>(), Integer::sum);

    public DiffTreeMiningResult() {
        this(0, 0, new DiffTreeSerializeDebugData(), new ExplainedFilterSummary());
    }

    public DiffTreeMiningResult(
            int exportedCommits,
            int exportedTrees,
            final DiffTreeSerializeDebugData debugData,
            final ExplainedFilterSummary filterHits)
    {
        this.exportedCommits = exportedCommits;
        this.exportedTrees = exportedTrees;
        this.debugData = debugData;
        this.filterHits = filterHits;
        this.atomicPatternCounts = new AtomicPatternCount();
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
    public static DiffTreeMiningResult importFrom(final Path p) throws IOException {
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
                case "trees" -> result.exportedTrees = Integer.parseInt(value);
                case "commits" -> result.exportedCommits = Integer.parseInt(value);
                case "#NON nodes" -> result.debugData.numExportedNonNodes = Integer.parseInt(value);
                case "#ADD nodes" -> result.debugData.numExportedAddNodes = Integer.parseInt(value);
                case "#REM nodes" -> result.debugData.numExportedRemNodes = Integer.parseInt(value);
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
                        // other lines that do not match
                        throw new IOException("unknown entry: " + line);
                    }
                }
            }
        }
        
        result.filterHits = ExplainedFilterSummary.parse(filterHitsLines);
        result.atomicPatternCounts = AtomicPatternCount.parse(atomicPatternCountsLines);
        
        return result;
    }

    @Override
    public LinkedHashMap<String, Object> snapshot() {
        LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
        snap.put("trees", exportedTrees);
        snap.put("commits", exportedCommits);
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

package main.mining;

import de.variantsync.functjonal.Functjonal;
import diff.difftree.serialize.DiffTreeSerializeDebugData;
import diff.result.DiffError;
import metadata.AtomicPatternCount;
import metadata.ExplainedFilterSummary;
import metadata.Metadata;
import org.pmw.tinylog.Logger;
import util.IO;
import util.semigroup.IntSum;
import util.semigroup.MergeMap;
import util.semigroup.Semigroup;
import util.semigroup.Unknown;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DiffTreeMiningResult implements Metadata<DiffTreeMiningResult> {
    public final static String EXTENSION = ".metadata.txt";

    public int exportedCommits;
    public int exportedTrees;
    public final DiffTreeSerializeDebugData debugData;
    public ExplainedFilterSummary filterHits;
    public AtomicPatternCount atomicPatternCounts;
    private final MergeMap<String, Unknown> customInfo = new MergeMap<String, Unknown>(new LinkedHashMap<>());
    private final MergeMap<DiffError, IntSum> diffErrors = new MergeMap<DiffError, IntSum>(new HashMap<>());

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

    public void putCustomInfo(final String key, final Semigroup<?> value) {
        customInfo.put(key, new Unknown(value));
    }

    public void putCustomInfo(final String key, final String value) {
        putCustomInfo(key, Semigroup.singleton(value));
    }

    public void reportDiffErrors(final List<DiffError> errors) {
        for (final DiffError e : errors) {
            diffErrors.put(e, new IntSum(1));
        }
    }

    @Override
    public void append(final DiffTreeMiningResult other) {
        exportedCommits += other.exportedCommits;
        exportedTrees += other.exportedTrees;
        debugData.append(other.debugData);
        filterHits.append(other.filterHits);
        atomicPatternCounts.append(other.atomicPatternCounts);
        customInfo.append(other.customInfo);
        diffErrors.append(other.diffErrors);
    }

    public String exportTo(final Path file) {
        try {
            final String result = Metadata.show(snapshot());
            IO.write(file, result);
            return result;
        } catch (IOException e) {
            Logger.error(e);
            System.exit(0);
            return "";
        }
    }

    @Override
    public LinkedHashMap<String, Object> snapshot() {
        // use LinkedHashMap to have insertion-order iteration
        LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
        snap.put("trees", exportedTrees);
        snap.put("commits", exportedCommits);
        snap.putAll(debugData.snapshot());
        snap.putAll(filterHits.snapshot());
        snap.putAll(atomicPatternCounts.snapshot());
        snap.putAll(customInfo);
        snap.putAll(Functjonal.bimap(diffErrors, error -> "#Error[" + error + "]", IntSum::toString));
        return snap;
    }
}

package main.mining;

import diff.difftree.serialize.DiffTreeSerializeDebugData;
import metadata.AtomicPatternCount;
import metadata.ExplainedFilterSummary;
import metadata.Metadata;
import org.pmw.tinylog.Logger;
import util.IO;
import util.functional.CollisionMap;
import util.functional.Semigroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public class DiffTreeMiningResult implements Metadata<DiffTreeMiningResult> {
    public final static String EXTENSION = ".metadata.txt";

    public int exportedCommits;
    public int exportedTrees;
    public final DiffTreeSerializeDebugData debugData;
    public ExplainedFilterSummary filterHits;
    public AtomicPatternCount atomicPatternCounts;

    private final CollisionMap<String, Semigroup<?>> customInfo = new CollisionMap<>(new LinkedHashMap<>());

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
        customInfo.put(key, value);
    }

    public void putCustomInfo(final String key, final String value) {
        putCustomInfo(key, Semigroup.singleton(value));
    }

    @Override
    public void append(final DiffTreeMiningResult other) {
        exportedCommits += other.exportedCommits;
        exportedTrees += other.exportedTrees;
        debugData.append(other.debugData);
        filterHits.append(other.filterHits);
        atomicPatternCounts.append(other.atomicPatternCounts);
        customInfo.append(other.customInfo);
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
        return snap;
    }
}

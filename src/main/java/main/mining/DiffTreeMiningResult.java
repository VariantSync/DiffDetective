package main.mining;

import diff.difftree.serialize.DiffTreeSerializeDebugData;
import metadata.ExplainedFilterSummary;
import metadata.Metadata;
import org.pmw.tinylog.Logger;
import util.IO;
import util.Semigroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiffTreeMiningResult implements Semigroup<DiffTreeMiningResult>, Metadata {
    public final static String EXTENSION = ".metadata.txt";

    public int exportedCommits;
    public int exportedTrees;
    public final DiffTreeSerializeDebugData debugData;
    public ExplainedFilterSummary filterHits;

    public <T> DiffTreeMiningResult() {
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
    }

    @Override
    public void append(final DiffTreeMiningResult other) {
        exportedCommits += other.exportedCommits;
        exportedTrees += other.exportedTrees;
        debugData.append(other.debugData);
        filterHits.append(other.filterHits);
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
    public Map<String, Object> snapshot() {
        // use LinkedHashMap to have insertion-order iteration
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("trees", exportedTrees);
        snap.put("commits", exportedCommits);
        snap.putAll(debugData.snapshot());
        snap.putAll(filterHits.snapshot());
        return snap;
    }
}

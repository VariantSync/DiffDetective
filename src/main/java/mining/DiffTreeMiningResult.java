package mining;

import de.variantsync.functjonal.Functjonal;
import diff.difftree.serialize.DiffTreeSerializeDebugData;
import diff.result.DiffError;
import metadata.AtomicPatternCount;
import metadata.ExplainedFilterSummary;
import metadata.Metadata;
import org.pmw.tinylog.Logger;
import util.IO;
import util.semigroup.InlineSemigroup;
import util.semigroup.MergeMap;
import util.semigroup.Semigroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DiffTreeMiningResult implements Metadata<DiffTreeMiningResult> {
    public final static String EXTENSION = ".metadata.txt";

    public final static InlineSemigroup<DiffTreeMiningResult> ISEMIGROUP = (a, b) -> {
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
        LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
        snap.put("trees", exportedTrees);
        snap.put("commits", exportedCommits);
        snap.putAll(debugData.snapshot());
        snap.putAll(filterHits.snapshot());
        snap.putAll(atomicPatternCounts.snapshot());
        snap.putAll(customInfo);
        snap.putAll(Functjonal.bimap(diffErrors, error -> "#Error[" + error + "]", Object::toString));
        return snap;
    }

    @Override
    public InlineSemigroup<DiffTreeMiningResult> semigroup() {
        return ISEMIGROUP;
    }
}

package analysis;

import metadata.Metadata;
import org.variantsync.functjonal.category.InplaceSemigroup;

import java.util.LinkedHashMap;

public record AutomationResult(
        long numMeasuredCommits,
        long totalTimeMS,
        CommitProcessTime fastest,
        CommitProcessTime slowest,
        CommitProcessTime median
) implements Metadata<AutomationResult> {
    public double avgTimeMS() {
        return ((double) totalTimeMS) / ((double) numMeasuredCommits);
    }

    public double totalTimeMinutes() {
        return ((double) totalTimeMS / 1000.0) / 60.0;
    }

    @Override
    public String toString() {
        return Metadata.show(snapshot());
    }

    @Override
    public LinkedHashMap<String, ?> snapshot() {
        final LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
        snap.put("#Commits", numMeasuredCommits);
        snap.put("Total   commit process time is", totalTimeMinutes() + "min");
        snap.put("Fastest commit process time is", fastest);
        snap.put("Slowest commit process time is", slowest);
        snap.put("Median  commit process time is", median);
        snap.put("Average commit process time is", avgTimeMS() + "ms");
        return snap;
    }

    @Override
    public InplaceSemigroup<AutomationResult> semigroup() {
        return null;
    }
}

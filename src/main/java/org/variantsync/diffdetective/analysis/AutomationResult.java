package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.functjonal.category.InplaceSemigroup;

import java.util.LinkedHashMap;

/**
 * Statistics on runtimes for commit processing.
 * @param numMeasuredCommits Number of measured commits.
 * @param totalTimeMS Total time in milliseconds that it took to process the commits.
 * @param fastest The commit that was processed that fastest.
 * @param slowest The commit that was processed that slowest.
 * @param median The median time it took to process a commit.
 * @author Paul Bittner
 */
public record AutomationResult(
        long numMeasuredCommits,
        long totalTimeMS,
        CommitProcessTime fastest,
        CommitProcessTime slowest,
        CommitProcessTime median
) implements Metadata<AutomationResult> {
    /**
     * @return The average time it took to process a commit in milliseconds.
     */
    public double avgTimeMS() {
        return ((double) totalTimeMS) / ((double) numMeasuredCommits);
    }

    /**
     * @return Total time in minutes that it took to process the commits.
     */
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

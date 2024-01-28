package org.variantsync.diffdetective.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.functjonal.category.InplaceSemigroup;

public class StatisticsAnalysis implements Analysis.Hooks {
    public static final String COMMIT_TIME_FILE_EXTENSION = ".committimes.txt";

    public static final ResultKey<Result> RESULT = new ResultKey<>("StatisticsAnalysis");

    /**
     * Invariant:
     * {@link Result#emptyCommits} + {@link Result#failedCommits} + {@link Result#processedCommits} - {@link FilterAnalysis filteredCommits} = {@link Analysis.TotalNumberOfCommitsResult#value}
     */
    public static final class Result implements Metadata<Result> {
        /**
         * Number of commits that were not processed because they had no VariationDiffs.
         * A commit is empty iff at least of one of the following conditions is met for every of its patches:
         * <ul>
         * <li>the patch did not edit a C file,
         * <li>the VariationDiff became empty after transformations (this can happen if there are only whitespace changes),
         * <li>or the patch had syntax errors in its annotations, so the VariationDiff could not be parsed.
         * </ul>
         */
        public int emptyCommits = 0;
        /**
         * Number of commits that could not be parsed at all because of exceptions when operating JGit.
         *
         * The number of commits that were filtered because they are a merge commit is thus given as
         * {@code filteredCommits = totalCommits - processedCommits - emptyCommits - failedCommits}
         */
        public int failedCommits = 0;
        public int processedCommits = 0;

        public int totalPatches = 0;
        public int processedPatches = 0;
        /**
         * The total runtime in seconds (irrespective of multithreading).
         */
        public double runtimeInSeconds = 0;
        /**
         * The commit that was processed the fastest.
         */
        public final CommitProcessTime min;
        /**
         * The commit that was processed the slowest.
         */
        public final CommitProcessTime max;

        public Result() {
            this(AnalysisResult.NO_REPO);
        }

        public Result(String repoName) {
            this.min = CommitProcessTime.Unknown(repoName, Long.MAX_VALUE);
            this.max = CommitProcessTime.Unknown(repoName, Long.MIN_VALUE);
        }

        public static final InplaceSemigroup<Result> ISEMIGROUP = (a, b) -> {
            a.emptyCommits += b.emptyCommits;
            a.failedCommits += b.failedCommits;
            a.processedCommits += b.processedCommits;
            a.totalPatches += b.totalPatches;
            a.processedPatches += b.processedPatches;
            a.runtimeInSeconds += b.runtimeInSeconds;
            a.min.set(CommitProcessTime.min(a.min, b.min));
            a.max.set(CommitProcessTime.max(a.max, b.max));
        };

        @Override
        public InplaceSemigroup<Result> semigroup() {
            return ISEMIGROUP;
        }

        @Override
        public LinkedHashMap<String, Object> snapshot() {
            LinkedHashMap<String, Object> snap = new LinkedHashMap<>();
            snap.put(MetadataKeys.FAILED_COMMITS, failedCommits);
            snap.put(MetadataKeys.EMPTY_COMMITS, emptyCommits);
            snap.put(MetadataKeys.PROCESSED_COMMITS, processedCommits);
            snap.put(MetadataKeys.TOTAL_PATCHES, totalPatches);
            snap.put(MetadataKeys.PROCESSED_PATCHES, processedPatches);
            snap.put(MetadataKeys.MINCOMMIT, min.toString());
            snap.put(MetadataKeys.MAXCOMMIT, max.toString());
            snap.put(MetadataKeys.RUNTIME, runtimeInSeconds);
            return snap;
        }

        @Override
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            failedCommits = Integer.parseInt(snap.get(MetadataKeys.FAILED_COMMITS));
            emptyCommits = Integer.parseInt(snap.get(MetadataKeys.EMPTY_COMMITS));
            processedCommits = Integer.parseInt(snap.get(MetadataKeys.PROCESSED_COMMITS));
            totalPatches = Integer.parseInt(snap.get(MetadataKeys.TOTAL_PATCHES));
            min.set(CommitProcessTime.fromString(snap.get(MetadataKeys.MINCOMMIT)));
            max.set(CommitProcessTime.fromString(snap.get(MetadataKeys.MAXCOMMIT)));
            processedPatches = Integer.parseInt(snap.get(MetadataKeys.PROCESSED_PATCHES));

            String runtime = snap.get(MetadataKeys.RUNTIME);
            if (runtime.endsWith("s")) {
                runtime = runtime.substring(0, runtime.length() - 1);
            }
            runtimeInSeconds = Double.parseDouble(runtime);
        }
    }

    // List to store the process time of each commit.
    private final List<CommitProcessTime> commitTimes = new ArrayList<>(Analysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
    // Clock for runtime measurement.
    private final Clock totalTime = new Clock();
    private final Clock commitProcessTimer = new Clock();
    private int numVariationDiffs = 0;

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(RESULT, new Result(analysis.getRepository().getRepositoryName()));
    }

    @Override
    public void beginBatch(Analysis analysis) {
        totalTime.start();
    }

    @Override
    public boolean beginCommit(Analysis analysis) {
        commitProcessTimer.start();
        numVariationDiffs = 0;
        return true;
    }

    @Override
    public boolean onParsedCommit(Analysis analysis) {
        analysis.get(RESULT).totalPatches += analysis.getCurrentCommitDiff().getPatchAmount();
        return true;
    }

    @Override
    public void onFailedCommit(Analysis analysis) {
        analysis.get(RESULT).failedCommits += 1;
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) {
        ++numVariationDiffs;
        return true;
    }

    @Override
    public void endCommit(Analysis analysis) {
        analysis.get(RESULT).processedPatches += numVariationDiffs;

        // Report the commit process time if the commit is not empty.
        if (numVariationDiffs > 0) {
            final long commitTimeMS = commitProcessTimer.getPassedMilliseconds();
            // find max commit time
            if (commitTimeMS > analysis.get(RESULT).max.milliseconds()) {
                analysis.get(RESULT).max.set(analysis.getCurrentCommitDiff().getCommitHash(), commitTimeMS);
            }
            // find min commit time
            if (commitTimeMS < analysis.get(RESULT).min.milliseconds()) {
                analysis.get(RESULT).min.set(analysis.getCurrentCommitDiff().getCommitHash(), commitTimeMS);
            }
            // report time
            commitTimes.add(new CommitProcessTime(analysis.getCurrentCommitDiff().getCommitHash(), analysis.getRepository().getRepositoryName(), commitTimeMS));
            analysis.get(RESULT).processedCommits += 1;
        } else {
            analysis.get(RESULT).emptyCommits += 1;
        }
    }

    @Override
    public void endBatch(Analysis analysis) throws IOException {
        // shutdown; report total time; export results
        analysis.get(RESULT).runtimeInSeconds = totalTime.getPassedSeconds();
        exportCommitTimes(commitTimes, FileUtils.addExtension(analysis.getOutputFile(), COMMIT_TIME_FILE_EXTENSION));
    }

    /**
     * Exports the given commit times to the given file. Overwrites existing files.
     * @param commitTimes List of all CommitProcessTimes to write into a single file.
     * @param pathToOutputFile Output file to write.
     */
    public static void exportCommitTimes(final List<CommitProcessTime> commitTimes, final Path pathToOutputFile) throws IOException {
        final StringBuilder times = new StringBuilder();

        for (final CommitProcessTime ct : commitTimes) {
            times.append(ct.toString()).append(StringUtils.LINEBREAK);
        }

        IO.write(pathToOutputFile, times.toString());
    }
}

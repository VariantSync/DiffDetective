package org.variantsync.diffdetective.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;

public class StatisticsAnalysis<T extends AnalysisResult<T>> implements Analysis.Hooks<T> {
    public static final String COMMIT_TIME_FILE_EXTENSION = ".committimes.txt";

    // List to store the process time of each commit.
    private final List<CommitProcessTime> commitTimes = new ArrayList<>(Analysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
    // Clock for runtime measurement.
    private final Clock totalTime = new Clock();
    private final Clock commitProcessTimer = new Clock();
    private int numDiffTrees = 0;

    @Override
    public void beginBatch(Analysis<T> analysis) {
        totalTime.start();
    }

    @Override
    public boolean beginCommit(Analysis<T> analysis) {
        commitProcessTimer.start();
        numDiffTrees = 0;
        return true;
    }

    @Override
    public boolean onParsedCommit(Analysis<T> analysis) {
        analysis.getResult().totalPatches += analysis.getCommitDiff().getPatchAmount();
        return true;
    }

    @Override
    public boolean analyzeDiffTree(Analysis<T> analysis) {
        ++numDiffTrees;
        return true;
    }

    @Override
    public void endCommit(Analysis<T> analysis) {
        analysis.getResult().exportedTrees += numDiffTrees;

        // Report the commit process time if the commit is not empty.
        if (numDiffTrees > 0) {
            final long commitTimeMS = commitProcessTimer.getPassedMilliseconds();
            // find max commit time
            if (commitTimeMS > analysis.getResult().max.milliseconds()) {
                analysis.getResult().max.set(analysis.getCommitDiff().getCommitHash(), commitTimeMS);
            }
            // find min commit time
            if (commitTimeMS < analysis.getResult().min.milliseconds()) {
                analysis.getResult().min.set(analysis.getCommitDiff().getCommitHash(), commitTimeMS);
            }
            // report time
            commitTimes.add(new CommitProcessTime(analysis.getCommitDiff().getCommitHash(), analysis.getRepository().getRepositoryName(), commitTimeMS));
            ++analysis.getResult().exportedCommits;
        } else {
            ++analysis.getResult().emptyCommits;
        }
    }

    @Override
    public void endBatch(Analysis<T> analysis) {
        // shutdown; report total time; export results
        analysis.getResult().runtimeInSeconds = totalTime.getPassedSeconds();
        analysis.getResult().exportTo(FileUtils.addExtension(analysis.getOutputFile(), CommitHistoryAnalysisResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(analysis.getOutputFile(), COMMIT_TIME_FILE_EXTENSION));
    }

    /**
     * Exports the given commit times to the given file. Overwrites existing files.
     * @param commitTimes List of all CommitProcessTimes to write into a single file.
     * @param pathToOutputFile Output file to write.
     */
    public static void exportCommitTimes(final List<CommitProcessTime> commitTimes, final Path pathToOutputFile) {
        final StringBuilder times = new StringBuilder();

        for (final CommitProcessTime ct : commitTimes) {
            times.append(ct.toString()).append(StringUtils.LINEBREAK);
        }

        try {
            IO.write(pathToOutputFile, times.toString());
        } catch (IOException e) {
            Logger.error(e);
            System.exit(1);
        }
    }
}

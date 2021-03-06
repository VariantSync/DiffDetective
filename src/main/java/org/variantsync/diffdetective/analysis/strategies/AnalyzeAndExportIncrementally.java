package org.variantsync.diffdetective.analysis.strategies;

import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.util.IO;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Collects the linegraph representations generated by an analysis and exports them once a certain threshold of
 * representations has been stored.
 * The default value is 100, meaning that a linegraph file is written for every 100 processed commits.
 * @author Paul Bittner
 */
public class AnalyzeAndExportIncrementally extends AnalysisStrategy {
    /**
     * Default value for the amount of commits whose linegraph representations should be exported together.
     */
    public static final int DEFAULT_NUMBER_OF_COMMITS_TO_EXPORT_AT_ONCE = 100;
    private final int commitsToExportAtOnce;

    private StringBuilder nextChunkToExport;
    private int collectedCommits;

    /**
     * Creates a new strategy that collects the linegraph representations of the given amount of commits and then
     * exports them together.
     * @param numberOfCommitsToExportAtOnce Amount of commits whose linegraph representations should be exported together.
     */
    public AnalyzeAndExportIncrementally(int numberOfCommitsToExportAtOnce) {
        this.commitsToExportAtOnce = numberOfCommitsToExportAtOnce;
    }

    /**
     * Creates a new strategy with the default value of commits to export together.
     * @see AnalyzeAndExportIncrementally#DEFAULT_NUMBER_OF_COMMITS_TO_EXPORT_AT_ONCE
     * @see AnalyzeAndExportIncrementally#AnalyzeAndExportIncrementally(int)
     */
    public AnalyzeAndExportIncrementally() {
        this(DEFAULT_NUMBER_OF_COMMITS_TO_EXPORT_AT_ONCE);
    }

    @Override
    public void start(Repository repo, Path outputPath, DiffTreeLineGraphExportOptions options) {
        super.start(repo, outputPath, options);

        IO.tryDeleteFile(outputPath);
        nextChunkToExport = new StringBuilder();
        collectedCommits = 0;
    }

    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {
        ++collectedCommits;
        nextChunkToExport.append(lineGraph);

        if (collectedCommits >= commitsToExportAtOnce) {
            exportAppend(outputPath, nextChunkToExport.toString());
            nextChunkToExport = new StringBuilder();
            collectedCommits = 0;
        }
    }

    @Override
    public void end() {
        if (!nextChunkToExport.isEmpty()) {
            exportAppend(outputPath, nextChunkToExport.toString());
        }
    }

    /**
     * Appends the given linegraph string at the end of the given file.
     * @param outputPath File to which the linegraph string should be appended.
     * @param linegraph String to append to the given file.
     */
    public static void exportAppend(final Path outputPath, final String linegraph) {
        try {
//            Logger.info("Writing file {}", outputPath);
            IO.append(outputPath, linegraph);
        } catch (IOException exception) {
            Logger.error(exception);
        }
    }
}

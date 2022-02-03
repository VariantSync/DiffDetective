package mining.strategies;

import datasets.Repository;
import diff.CommitDiff;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;

public class MineAndExportIncrementally extends DiffTreeMiningStrategy {
    public static final int DEFAULT_NUMBER_OF_COMMITS_TO_EXPORT_AT_ONCE = 100;
    private final int commitsToExportAtOnce;

    private StringBuilder nextChunkToExport;
    private int collectedCommits;

    public MineAndExportIncrementally(int numberOfCommitsToExportAtOnce) {
        this.commitsToExportAtOnce = numberOfCommitsToExportAtOnce;
    }

    public MineAndExportIncrementally() {
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

    public static void exportAppend(final Path outputPath, final String linegraph) {
        try {
//            Logger.info("Writing file " + outputPath);
            IO.append(outputPath, linegraph);
        } catch (IOException exception) {
            Logger.error(exception);
        }
    }
}

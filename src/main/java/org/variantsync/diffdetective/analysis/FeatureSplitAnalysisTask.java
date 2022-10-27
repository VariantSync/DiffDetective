package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class FeatureSplitAnalysisTask implements Callable<FeatureSplitResult> {
    public static final String COMMIT_TIME_FILE_EXTENSION = ".committimes.txt";
    public static final String PATCH_STATISTICS_EXTENSION = ".patchStatistics.csv";

    public record Options(
        Repository repository,
        GitDiffer differ,
        Path outputPath,
        DiffTreeLineGraphExportOptions exportOptions,
        AnalysisStrategy miningStrategy,
        Iterable<RevCommit> commits
    ) {}

    protected final Options options;

    protected FeatureSplitAnalysisTask(final Options options) {
        this.options = options;
    }

    public FeatureSplitAnalysisTask.Options getOptions() {
        return options;
    }

    @Override
    public FeatureSplitResult call() throws Exception {
        options.miningStrategy().start(options.repository(), options.outputPath(), options.exportOptions());

        final FeatureSplitResult miningResult = new FeatureSplitResult(options.repository.getRepositoryName());
        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();

        miningResult.putCustomInfo(MetadataKeys.TREEFORMAT, exportOptions.treeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.NODEFORMAT, exportOptions.nodeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.EDGEFORMAT, exportOptions.edgeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.TASKNAME, this.getClass().getName());

        return miningResult;
    }

    public static void exportCommitTimes(final List<CommitProcessTime> commitTimes, final Path pathToOutputFile) {
        final StringBuilder times = new StringBuilder();

        for (final CommitProcessTime ct : commitTimes) {
            times.append(ct.toString()).append(StringUtils.LINEBREAK);
        }

        try {
            IO.write(pathToOutputFile, times.toString());
        } catch (IOException e) {
            Logger.error(e);
            System.exit(0);
        }
    }

    public static void exportPatchStatistics(final List<PatchStatistics> commitTimes, final Path pathToOutputFile) {
        final String csv = CSV.toCSV(commitTimes);

        try {
            IO.write(pathToOutputFile, csv);
        } catch (IOException e) {
            Logger.error(e);
            System.exit(0);
        }
    }
}

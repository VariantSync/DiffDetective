package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Abstract base class for tasks to run during a {@link HistoryAnalysis}.
 * A <code>CommitHistoryAnalysisTask</code>s purpose is to process a given set of commits with a specific analysis.
 * @author Paul Bittner
 */
public abstract class CommitHistoryAnalysisTask implements Callable<AnalysisResult> {
    public static final String COMMIT_TIME_FILE_EXTENSION = ".committimes.txt";
    public static final String PATCH_STATISTICS_EXTENSION = ".patchStatistics.csv";

    /**
     * Options that may be specified for processing a set of commits.
     * @param repository The repository that is analyzed.
     * @param differ The differ that should be used to obtain diffs.
     * @param outputDir The path to which any output should be written on disk.
     * @param treeFilter filters commits before processing them
     * @param treePreProcessing applies a processing function after filtering, but before processing
     * @param analysisStrategy A callback that is invoked for each commit.
     * @param commits The set of commits to process in this task.
     */
    public record Options(
        Repository repository,
        GitDiffer differ,
        Path outputDir,
        ExplainedFilter<DiffTree> treeFilter,
        List<DiffTreeTransformer> treePreProcessing,
        AnalysisStrategy analysisStrategy,
        Iterable<RevCommit> commits
    ) {}

    protected final Options options;

    protected CommitHistoryAnalysisTask(final Options options) {
        this.options = options;
    }

    /**
     * Returns the options for this task.
     * @return the options for this task.
     */
    public CommitHistoryAnalysisTask.Options getOptions() {
        return options;
    }

    @Override
    public AnalysisResult call() throws Exception {
        options.analysisStrategy().start(options.repository(), options.outputDir());

        final AnalysisResult miningResult = new AnalysisResult(options.repository.getRepositoryName());
        miningResult.putCustomInfo(MetadataKeys.TASKNAME, this.getClass().getName());

        return miningResult;
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

    /**
     * Exports the given patch statistics to the given file. Overwrites existing files.
     * @param commitTimes List of all PatchStatistics to write into a single file.
     * @param pathToOutputFile Output file to write.
     */
    public static void exportPatchStatistics(final List<PatchStatistics> commitTimes, final Path pathToOutputFile) {
        final String csv = CSV.toCSV(commitTimes);

        try {
            IO.write(pathToOutputFile, csv);
        } catch (IOException e) {
            Logger.error(e);
            System.exit(1);
        }
    }
}

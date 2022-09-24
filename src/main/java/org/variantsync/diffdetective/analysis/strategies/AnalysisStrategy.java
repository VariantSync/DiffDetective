package org.variantsync.diffdetective.analysis.strategies;

import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;

import java.nio.file.Path;

/**
 * Callbacks for {@link org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask}.
 * A strategy may perform arbitrary additional tasks upon the execution of a task.
 * The strategy is notified about the start and end of a task as well after each processed commit.
 * @author Paul Bittner
 */
public abstract class AnalysisStrategy {
    protected Repository repo;
    protected Path outputPath;
    protected LineGraphExportOptions exportOptions;

    /**
     * Invoked when the analysis starts.
     *
     * @param repo The repository on which an analysis is performed.
     * @param outputPath A directory to which output should be written.
     * @param options Options for data export.
     */
    public void start(Repository repo, Path outputPath, LineGraphExportOptions options) {
        this.repo = repo;
        this.outputPath = outputPath;
        this.exportOptions = options;
    }

    /**
     * Invoked whenever the analysis processed a commit and converted it to linegraph format.
     * @param commit The commit that was just processed.
     * @param lineGraph The linegraph representation of the processed commit. Might be empty if no export to linegraph is desired.
     */
    public abstract void onCommit(CommitDiff commit, String lineGraph);

    /**
     * Invoked when the analysis is done for the current repository.
     * The analysis might restart with another repository.
     * In this case, {@link AnalysisStrategy#start} is invoked again.
     */
    public abstract void end();
}

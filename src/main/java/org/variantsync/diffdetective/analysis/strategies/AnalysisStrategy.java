package org.variantsync.diffdetective.analysis.strategies;

import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;

import java.io.OutputStream;
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

    /**
     * Invoked when the analysis starts.
     *
     * @param repo The repository on which an analysis is performed.
     * @param outputPath A directory to which output should be written.
     */
    public void start(Repository repo, Path outputPath) {
        this.repo = repo;
        this.outputPath = outputPath;
    }

    /**
     * Invoked before a commit is analyzed.
     *
     * The returned line graph export destination is closed after processing the commit given by
     * {@code commit}.
     *
     * @param commit The commit that was just processed.
     * @return the line graph export destination
     */
    public abstract OutputStream onCommit(CommitDiff commit);

    /**
     * Invoked when the analysis is done for the current repository.
     * The analysis might restart with another repository.
     * In this case, {@link AnalysisStrategy#start} is invoked again.
     */
    public abstract void end();
}

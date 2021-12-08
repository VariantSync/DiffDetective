package main.mining;

import datasets.Repository;
import diff.CommitDiff;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;

import java.nio.file.Path;

public abstract class DiffTreeMiningStrategy {
    protected Repository repo;
    protected Path outputPath;
    protected DiffTreeLineGraphExportOptions exportOptions;

    /**
     * Invoked when mining starts.
     *
     * @param repo The repository on which the mining is performed.
     * @param outputPath A directory to which output should be put.
     * @param options Options for data export.
     */
    public void start(Repository repo, Path outputPath, DiffTreeLineGraphExportOptions options) {
        this.repo = repo;
        this.outputPath = outputPath;
        this.exportOptions = options;
    }

    /**
     * Invoked whenever the miner processed a commit and converted it to linegraph format.
     * @param commit The commit that was just processed.
     * @param lineGraph The linegraph representation of the processed commit.
     */
    public abstract void onCommit(CommitDiff commit, String lineGraph);

    /**
     * Invoked when mining is done for the current repository.
     * The miner might restart the mining with another repository then.
     * In this case, {@link DiffTreeMiningStrategy::start} is invoked again.
     */
    public abstract void end();
}

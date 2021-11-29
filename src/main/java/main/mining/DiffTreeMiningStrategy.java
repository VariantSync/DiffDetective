package main.mining;

import datasets.Repository;
import diff.CommitDiff;
import diff.serialize.LineGraphExport;

import java.nio.file.Path;

public abstract class DiffTreeMiningStrategy {
    protected Repository repo;
    protected Path outputPath;
    protected LineGraphExport.Options exportOptions;

    public void start(Repository repo, Path outputPath, LineGraphExport.Options options) {
        this.repo = repo;
        this.outputPath = outputPath;
        this.exportOptions = options;
    }

    public abstract void onCommit(CommitDiff commit, String lineGraph);

    public abstract String end();
}

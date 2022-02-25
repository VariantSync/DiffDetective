package mining.strategies;

import datasets.Repository;
import diff.GitDiffer;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import mining.DiffTreeMiningResult;
import mining.MetadataKeys;
import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public abstract class CommitHistoryAnalysisTask implements Callable<DiffTreeMiningResult> {
    public record Options(
        Repository repository,
        GitDiffer differ,
        Path outputPath,
        DiffTreeLineGraphExportOptions exportOptions,
        DiffTreeMiningStrategy miningStrategy,
        Iterable<RevCommit> commits
    ) {}

    protected final Options options;

    protected CommitHistoryAnalysisTask(final Options options) {
        this.options = options;
    }

    public CommitHistoryAnalysisTask.Options getOptions() {
        return options;
    }

    @Override
    public DiffTreeMiningResult call() throws Exception {
        options.miningStrategy().start(options.repository(), options.outputPath(), options.exportOptions());

        final DiffTreeMiningResult miningResult = new DiffTreeMiningResult();
        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();

        miningResult.putCustomInfo(MetadataKeys.TREEFORMAT, exportOptions.treeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.NODEFORMAT, exportOptions.nodeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.EDGEFORMAT, exportOptions.edgeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.TASKNAME, this.getClass().getName());

        return miningResult;
    }
}

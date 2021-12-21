package main.mining;

import datasets.Repository;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.DiffTreeSerializeDebugData;
import diff.difftree.serialize.LineGraphExport;
import main.mining.strategies.DiffTreeMiningStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import util.FileUtils;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public record MiningTask(
        Repository repository,
        GitDiffer differ,
        Path outputPath,
        DiffTreeLineGraphExportOptions exportOptions,
        DiffTreeMiningStrategy miningStrategy,
        Iterable<RevCommit> commits
) implements Callable<DiffTreeMiningResult> {
    @Override
    public DiffTreeMiningResult call() throws Exception {
        miningStrategy.start(repository, outputPath, exportOptions);
        final DiffTreeMiningResult miningResult = new DiffTreeMiningResult();
        for (final RevCommit commit : commits) {
            final CommitDiff commitDiff = differ.createCommitDiff(commit);

            final StringBuilder lineGraph = new StringBuilder();
            final Pair<DiffTreeSerializeDebugData, Integer> res = LineGraphExport.toLineGraphFormat(commitDiff, lineGraph, exportOptions);
            miningStrategy.onCommit(commitDiff, lineGraph.toString());

            ++miningResult.exportedCommits;
            miningResult.exportedTrees += res.getValue();
            miningResult.debugData.mappend(res.getKey());
        }

        miningStrategy.end();
        miningResult.exportTo(FileUtils.addExtension(outputPath, DiffTreeMiningResult.EXTENSION));
        return miningResult;
    }
}

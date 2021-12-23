package main.mining;

import datasets.Repository;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.LineGraphExport;
import diff.result.CommitDiffResult;
import main.mining.strategies.DiffTreeMiningStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.pmw.tinylog.Logger;
import pattern.atomic.proposed.ProposedAtomicPatterns;
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
        miningResult.putCustomInfo("treeformat", exportOptions.treeFormat().getName());
        miningResult.putCustomInfo("nodeformat", exportOptions.nodeFormat().getName());

        for (final RevCommit commit : commits) {
            final CommitDiffResult commitDiffResult = differ.createCommitDiff(commit);

            miningResult.reportDiffErrors(commitDiffResult.unwrap().second());
            if (commitDiffResult.unwrap().first().isEmpty()) {
                Logger.debug("[MiningTask::call] found commit that failed entirely and was not filtered because:\n" + commitDiffResult.unwrap().second());
                continue;
            }

            final CommitDiff commitDiff = commitDiffResult.unwrap().first().get();
            final StringBuilder lineGraph = new StringBuilder();
            miningResult.append(LineGraphExport.toLineGraphFormat(commitDiff, lineGraph, exportOptions));
            miningStrategy.onCommit(commitDiff, lineGraph.toString());
            exportOptions.treeFilter().resetExplanations();

            // Count atomic patterns
            for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                if (patch.isValid()) {
                    final DiffTree t = patch.getDiffTree();
                    t.forAll(node -> {
                        if (node.isCode()) {
                            miningResult.atomicPatternCounts.reportOccurrenceFor(
                                    ProposedAtomicPatterns.Instance.match(node),
                                    commitDiff
                            );
                        }
                    });
                }
            }
        }

        miningStrategy.end();
        miningResult.exportTo(FileUtils.addExtension(outputPath, DiffTreeMiningResult.EXTENSION));
        return miningResult;
    }
}

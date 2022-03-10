package mining.strategies;

import diff.CommitDiff;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.serialize.LineGraphExport;
import diff.result.CommitDiffResult;
import mining.DiffTreeMiningResult;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Clock;
import util.FileUtils;

public class MiningTask extends CommitHistoryAnalysisTask {
    public MiningTask(final Options options) {
        super(options);
    }

    @Override
    public DiffTreeMiningResult call() throws Exception {
        final DiffTreeMiningResult miningResult = super.call();

        final Clock totalTime = new Clock();
        totalTime.start();

        for (final RevCommit commit : options.commits()) {
            final CommitDiffResult commitDiffResult = options.differ().createCommitDiff(commit);

            miningResult.reportDiffErrors(commitDiffResult.errors());
            if (commitDiffResult.diff().isEmpty()) {
                Logger.debug("[MiningTask::call] found commit that failed entirely and was not filtered because:\n" + commitDiffResult.errors());
                continue;
            }

            /*
             * We export all difftrees that match our filter criteria (e.g., has more than one atomic pattern).
             * However, we count atomic patterns of all DiffTrees, even those that are not exported to Linegraph.
             */
            final CommitDiff commitDiff = commitDiffResult.diff().get();
            final StringBuilder lineGraph = new StringBuilder();
            miningResult.append(LineGraphExport.toLineGraphFormat(commitDiff, lineGraph, options.exportOptions()));
            options.miningStrategy().onCommit(commitDiff, lineGraph.toString());
            options.exportOptions().treeFilter().resetExplanations();

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

        options.miningStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputPath(), DiffTreeMiningResult.EXTENSION));
        return miningResult;
    }
}

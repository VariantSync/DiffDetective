package org.variantsync.diffdetective.mining;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExport;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;

public class MiningTask extends CommitHistoryAnalysisTask {
    public MiningTask(final Options options) {
        super(options);
    }

    @Override
    public AnalysisResult call() throws Exception {
        final AnalysisResult miningResult = super.call();

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
             * We export all difftrees that match our filter criteria (e.g., has more than one elementary pattern).
             * However, we count elementary patterns of all DiffTrees, even those that are not exported to Linegraph.
             */
            final CommitDiff commitDiff = commitDiffResult.diff().get();
            final StringBuilder lineGraph = new StringBuilder();
            miningResult.append(LineGraphExport.toLineGraphFormat(commitDiff, lineGraph, options.exportOptions()));
            options.miningStrategy().onCommit(commitDiff, lineGraph.toString());
            options.exportOptions().treeFilter().resetExplanations();

            // Count elementary patterns
            for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                if (patch.isValid()) {
                    final DiffTree t = patch.getDiffTree();
                    t.forAll(node -> {
                        if (node.isCode()) {
                            miningResult.elementaryPatternCounts.reportOccurrenceFor(
                                    ProposedElementaryPatterns.Instance.match(node),
                                    commitDiff
                            );
                        }
                    });
                }
            }
        }

        options.miningStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputPath(), AnalysisResult.EXTENSION));
        return miningResult;
    }
}

package mining.strategies;

import diff.CommitDiff;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.transform.DiffTreeTransformer;
import diff.result.CommitDiffResult;
import metadata.ExplainedFilterSummary;
import mining.DiffTreeMiningResult;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Clock;
import util.FileUtils;

public class PatternValidation extends CommitHistoryAnalysisTask {
    public PatternValidation(Options options) {
        super(options);
    }

    @Override
    public DiffTreeMiningResult call() throws Exception {
        final DiffTreeMiningResult miningResult = super.call();
        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();

        Clock commitProcessTimer = new Clock();
        for (final RevCommit commit : options.commits()) {
            commitProcessTimer.start();

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
            options.miningStrategy().onCommit(commitDiff, "");

            // Count atomic patterns
            for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                if (patch.isValid()) {
                    final DiffTree t = patch.getDiffTree();
                    DiffTreeTransformer.apply(exportOptions.treePreProcessing(), t);
                    t.assertConsistency();

                    if (!exportOptions.treeFilter().test(t)) {
                        continue;
                    }

                    t.forAll(node -> {
                        if (node.isCode()) {
                            miningResult.atomicPatternCounts.reportOccurrenceFor(
                                    ProposedAtomicPatterns.Instance.match(node),
                                    commitDiff
                            );
                        }
                    });

                    ++miningResult.exportedTrees;
                }
            }

            final long commitTimeMS = commitProcessTimer.getPassedMilliseconds();
            if (commitTimeMS > miningResult.max.milliseconds()) {
                miningResult.max.set(commitDiff.getCommitHash(), commitTimeMS);
            }
            if (commitTimeMS < miningResult.min.milliseconds()) {
                miningResult.min.set(commitDiff.getCommitHash(), commitTimeMS);
            }

            ++miningResult.exportedCommits;
            miningResult.filterHits.append(new ExplainedFilterSummary(exportOptions.treeFilter()));
            exportOptions.treeFilter().resetExplanations();
        }

        options.miningStrategy().end();
        miningResult.exportTo(FileUtils.addExtension(options.outputPath(), DiffTreeMiningResult.EXTENSION));
        return miningResult;
    }
}

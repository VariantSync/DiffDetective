package mining.strategies;

import diff.CommitDiff;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.transform.DiffTreeTransformer;
import diff.result.CommitDiffResult;
import metadata.ExplainedFilterSummary;
import mining.DiffTreeMiner;
import mining.DiffTreeMiningResult;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Clock;
import util.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class PatternValidation extends CommitHistoryAnalysisTask {
    public PatternValidation(Options options) {
        super(options);
    }

    @Override
    public DiffTreeMiningResult call() throws Exception {
        final DiffTreeMiningResult miningResult = super.call();
        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();
        final List<CommitProcessTime> commitTimes = new ArrayList<>(DiffTreeMiner.COMMITS_TO_PROCESS_PER_THREAD);
        final Clock totalTime = new Clock();
        totalTime.start();
        final Clock commitProcessTimer = new Clock();

        for (final RevCommit commit : options.commits()) {
            try {
                commitProcessTimer.start();

                final CommitDiffResult commitDiffResult = options.differ().createCommitDiff(commit);

                miningResult.reportDiffErrors(commitDiffResult.errors());
                if (commitDiffResult.diff().isEmpty()) {
                    Logger.debug("[MiningTask::call] found commit that failed entirely and was not filtered because:\n" + commitDiffResult.errors());
                    ++miningResult.failedCommits;
                    continue;
                }

                final CommitDiff commitDiff = commitDiffResult.diff().get();
                options.miningStrategy().onCommit(commitDiff, "");

                // Count atomic patterns
                int numDiffTrees = 0;
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

                        ++numDiffTrees;
                    }
                }
                miningResult.exportedTrees += numDiffTrees;
                miningResult.filterHits.append(new ExplainedFilterSummary(exportOptions.treeFilter()));
                exportOptions.treeFilter().resetExplanations();

                // Only consider non-empty commits
                if (numDiffTrees > 0) {
                    final long commitTimeMS = commitProcessTimer.getPassedMilliseconds();
                    if (commitTimeMS > miningResult.max.milliseconds()) {
                        miningResult.max.set(commitDiff.getCommitHash(), commitTimeMS);
                    }
                    if (commitTimeMS < miningResult.min.milliseconds()) {
                        miningResult.min.set(commitDiff.getCommitHash(), commitTimeMS);
                    }
                    commitTimes.add(new CommitProcessTime(commitDiff.getCommitHash(), options.repository().getRepositoryName(), commitTimeMS));
                    ++miningResult.exportedCommits;
                } else {
                    ++miningResult.emptyCommits;
                }

            } catch (Exception e) {
                Logger.error(e);
                Logger.error("An unexpected error occurred at " + commit.getId().getName() + " in " + getOptions().repository().getRepositoryName() + "!");
                throw e;
            }
        }

        options.miningStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputPath(), DiffTreeMiningResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(options.outputPath(), COMMIT_TIME_FILE_EXTENSION));
        return miningResult;
    }
}

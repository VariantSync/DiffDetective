package org.variantsync.diffdetective.validation;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask;
import org.variantsync.diffdetective.analysis.CommitProcessTime;
import org.variantsync.diffdetective.analysis.HistoryAnalysis;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Task for performing the ESEC/FSE'22 validation on a set of commits from a given repository.
 * @author Paul Bittner
 */
public class EditClassValidationTask extends CommitHistoryAnalysisTask {
    public EditClassValidationTask(Options options) {
        super(options);
    }

    @Override
    public AnalysisResult call() throws Exception {
        // Setup. Obtain the result from the initial setup in the super class.
        final AnalysisResult miningResult = super.call();
        // List to store the process time of each commit.
        final List<CommitProcessTime> commitTimes = new ArrayList<>(HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
        // Clock for runtime measurement.
        final Clock totalTime = new Clock();
        totalTime.start();
        final Clock commitProcessTimer = new Clock();

        // For each commit:
        for (final RevCommit commit : options.commits()) {
            try {
                commitProcessTimer.start();

                // parse the commit
                final CommitDiffResult commitDiffResult = options.differ().createCommitDiff(commit);

                // report any errors that occurred and exit in case no DiffTree could be parsed.
                miningResult.reportDiffErrors(commitDiffResult.errors());
                if (commitDiffResult.diff().isEmpty()) {
                    Logger.debug("[MiningTask::call] found commit that failed entirely and was not filtered because:\n{}", commitDiffResult.errors());
                    ++miningResult.failedCommits;
                    continue;
                }

                // extract the produced commit diff and inform the strategy
                final CommitDiff commitDiff = commitDiffResult.diff().get();
                options.analysisStrategy().onCommit(commitDiff).close();

                // Count edit class matches
                int numDiffTrees = 0;
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(options.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!options.treeFilter().test(t)) {
                            continue;
                        }

                        t.forAll(node -> {
                            if (node.isArtifact()) {
                                miningResult.editClassCounts.reportOccurrenceFor(
                                        ProposedEditClasses.Instance.match(node),
                                        commitDiff
                                );
                            }
                        });

                        ++numDiffTrees;
                    }
                }
                miningResult.exportedTrees += numDiffTrees;
                miningResult.filterHits.append(new ExplainedFilterSummary(options.treeFilter()));
                options.treeFilter().resetExplanations();

                // Report the commit process time if the commit is not empty.
                if (numDiffTrees > 0) {
                    final long commitTimeMS = commitProcessTimer.getPassedMilliseconds();
                    // find max commit time
                    if (commitTimeMS > miningResult.max.milliseconds()) {
                        miningResult.max.set(commitDiff.getCommitHash(), commitTimeMS);
                    }
                    // find min commit time
                    if (commitTimeMS < miningResult.min.milliseconds()) {
                        miningResult.min.set(commitDiff.getCommitHash(), commitTimeMS);
                    }
                    // report time
                    commitTimes.add(new CommitProcessTime(commitDiff.getCommitHash(), options.repository().getRepositoryName(), commitTimeMS));
                    ++miningResult.exportedCommits;
                } else {
                    ++miningResult.emptyCommits;
                }

            } catch (Exception e) {
                Logger.error(e, "An unexpected error occurred at {} in {}", commit.getId().getName(), getOptions().repository().getRepositoryName());
                throw e;
            }
        }

        // shutdown; report total time; export results
        options.analysisStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputDir(), AnalysisResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(options.outputDir(), COMMIT_TIME_FILE_EXTENSION));
        return miningResult;
    }
}

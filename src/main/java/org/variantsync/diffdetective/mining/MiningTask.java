package org.variantsync.diffdetective.mining;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.*;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExport;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class MiningTask extends AnalysisTask<CommitHistoryAnalysisResult> {
    public MiningTask(final Options options) {
        super(options);
    }

    @Override
    public CommitHistoryAnalysisResult call() throws Exception {
        final var miningResult = new CommitHistoryAnalysisResult(options.repository().getRepositoryName());
        initializeResult(miningResult);

        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();

        final Clock totalTime = new Clock();

        final List<CommitProcessTime> commitTimes = new ArrayList<>(Analysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
        final List<PatchStatistics> patchStatistics = new ArrayList<>(Analysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
        final Clock commitProcessTimer = new Clock();

        totalTime.start();

        for (final RevCommit commit : options.commits()) {
            commitProcessTimer.start();
            final CommitDiffResult commitDiffResult = options.differ().createCommitDiff(commit);

            miningResult.reportDiffErrors(commitDiffResult.errors());
            if (commitDiffResult.diff().isEmpty()) {
                Logger.debug("found commit that failed entirely and was not filtered because:\n{}", commitDiffResult.errors());
                continue;
            }

            /*
             * We export all difftrees that match our filter criteria (e.g., match more than one edit class).
             * However, we count edit classes of all DiffTrees, even those that are not exported to Linegraph.
             */
            final CommitDiff commitDiff = commitDiffResult.diff().get();
            final StringBuilder lineGraph = new StringBuilder();
            miningResult.append(LineGraphExport.toLineGraphFormat(commitDiff, lineGraph, options.exportOptions()));
            options.analysisStrategy().onCommit(commitDiff, lineGraph.toString());
            options.exportOptions().treeFilter().resetExplanations();

            // Count edit classes
            int numDiffTrees = 0;
            for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                final PatchStatistics thisPatchesStatistics = new PatchStatistics(patch, ProposedEditClasses.Instance);

                if (patch.isValid()) {
                    final DiffTree t = patch.getDiffTree();
                    DiffTreeTransformer.apply(exportOptions.treePreProcessing(), t);
                    t.assertConsistency();

                    if (!exportOptions.treeFilter().test(t)) {
                        continue;
                    }

                    t.forAll(node -> {
                        if (node.isArtifact()) {
                            final EditClass editClass = ProposedEditClasses.Instance.match(node);
                            miningResult.editClassCounts.reportOccurrenceFor(
                                    editClass,
                                    commitDiff
                            );
                            thisPatchesStatistics.editClassCount().increment(editClass);
                        }
                    });

                    ++numDiffTrees;
                }

                patchStatistics.add(thisPatchesStatistics);
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
        }

        options.analysisStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputPath(), CommitHistoryAnalysisResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(options.outputPath(), COMMIT_TIME_FILE_EXTENSION));
        exportPatchStatistics(patchStatistics, FileUtils.addExtension(options.outputPath(), PATCH_STATISTICS_EXTENSION));
        return miningResult;
    }
}

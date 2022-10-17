package org.variantsync.diffdetective.mining;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.*;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;
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

public class MiningTask extends CommitHistoryAnalysisTask {
    private final LineGraphExportOptions exportOptions;

    public MiningTask(final Options options, final LineGraphExportOptions exportOptions) {
        super(options);

        this.exportOptions = exportOptions;
    }

    @Override
    public AnalysisResult call() throws Exception {
        final AnalysisResult miningResult = super.call();
        miningResult.putCustomInfo(MetadataKeys.TREEFORMAT, exportOptions.treeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.NODEFORMAT, exportOptions.nodeFormat().getName());
        miningResult.putCustomInfo(MetadataKeys.EDGEFORMAT, exportOptions.edgeFormat().getName());

        final Clock totalTime = new Clock();

        final List<CommitProcessTime> commitTimes = new ArrayList<>(HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
        final List<PatchStatistics> patchStatistics = new ArrayList<>(HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
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
             * We count the edit classes of all difftrees that match our filter criteria
             * (e.g., match more than one edit class) and export them to the destination
             * determined by the AnalysisStrategy.
             */
            int numDiffTrees = 0;
            final CommitDiff commitDiff = commitDiffResult.diff().get();
            try (var lineGraphDestination = options.analysisStrategy().onCommit(commitDiff)) {
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    final PatchStatistics thisPatchesStatistics = new PatchStatistics(patch, ProposedEditClasses.Instance);

                    if (patch.isValid()) {
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(options.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!options.treeFilter().test(t)) {
                            continue;
                        }

                        miningResult.append(LineGraphExport.toLineGraphFormat(miningResult.repoName, patch, exportOptions, lineGraphDestination));

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
            }

            miningResult.exportedCommits += 1;
            miningResult.exportedTrees += numDiffTrees;
            miningResult.filterHits.append(new ExplainedFilterSummary(options.treeFilter()));
            options.treeFilter().resetExplanations();

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
        miningResult.exportTo(FileUtils.addExtension(options.outputDir(), AnalysisResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(options.outputDir(), COMMIT_TIME_FILE_EXTENSION));
        exportPatchStatistics(patchStatistics, FileUtils.addExtension(options.outputDir(), PATCH_STATISTICS_EXTENSION));
        return miningResult;
    }
}

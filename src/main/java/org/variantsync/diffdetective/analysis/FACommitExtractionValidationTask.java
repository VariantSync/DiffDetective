package org.variantsync.diffdetective.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.difftree.transform.FeatureSplit;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;

public class FACommitExtractionValidationTask extends AnalysisTask<FeatureSplitResult> {
    Set<String> randomFeatures;
    public FACommitExtractionValidationTask(Options options, Set<String> randomFeatures) {
        super(options);
    }

    @Override
    public FeatureSplitResult call() throws Exception {
        final var miningResult = new FeatureSplitResult(options.repository().getRepositoryName());
        initializeResult(miningResult);

        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();
        final List<CommitProcessTime> commitTimes = new ArrayList<>(Analysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
        final Clock totalTime = new Clock();
        totalTime.start();
        final Clock commitProcessTimer = new Clock();

        for (final RevCommit commit : options.commits()) {
            try {
                commitProcessTimer.start();

                final CommitDiffResult commitDiffResult = options.differ().createCommitDiff(commit);

                // Tracking failed commits
                miningResult.reportDiffErrors(commitDiffResult.errors());
                if (commitDiffResult.diff().isEmpty()) {
                    Logger.debug("[MiningTask::call] found commit that failed entirely and was not filtered because:\n{}", commitDiffResult.errors());
                    ++miningResult.failedCommits;
                    continue;
                }
                // track successful commits
                ++miningResult.totalCommits;

                final CommitDiff commitDiff = commitDiffResult.diff().get();
                options.analysisStrategy().onCommit(commitDiff, "");
                miningResult.totalPatches += commitDiff.getPatchAmount();

                // inspect every patch
                int numDiffTrees = 0;
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {

                        // generate TreeDiff
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(exportOptions.treePreProcessing(), t);
                        t.assertConsistency();
                        //miningResult.treeDiffSizes = miningResult.treeDiffSizes >= t.computeSize() ? miningResult.treeDiffSizes : t.computeSize();

                        if (!exportOptions.treeFilter().test(t)) {
                            continue;
                        }
                        
                        // Add features to results
                        randomFeatures.forEach(feature -> {
                            if (miningResult.totalFeatureAwarePatches.get(feature) == null) {
                                miningResult.totalFeatureAwarePatches.put(feature, 0);
                                miningResult.totalRemainderPatches.put(feature, 0);
                            }
                        });
                        
                        // validate FeatureSplit
                        randomFeatures.forEach(feature -> {

                            System.out.println(t.toString());
                            System.out.println(t.computeSize());

                            // generate feature-aware and remaining patches
                            HashMap<String, DiffTree> featureAware = FeatureSplit.featureSplit(t, PropositionalFormulaParser.Default.parse(feature));
                            System.out.println("FeatureSplit");

                            // 1. get number of feature-aware patches for a patch
                            if(featureAware.get(feature) != null) miningResult.totalFeatureAwarePatches.replace(feature, miningResult.totalFeatureAwarePatches.get(feature) + 1);
                            if(featureAware.get("remains") != null) miningResult.totalRemainderPatches.replace(feature, miningResult.totalRemainderPatches.get(feature) + 1);
                            

                            featureAware.forEach((key, value) -> {
                                if (value == null) return;
                                // 2. get calculation time for a patch with committimes.txt!!

                                // 3. check if patch is valid
                                if(!value.isConsistent().isSuccess())  {
                                    Logger.error("incorrectly extracted tree");
                                    ++miningResult.invalidFADiff;
                                }

                                // 4. calculate size of feature-aware difftree and size of initial difftree
                                int featureDiffSizeRatio = value.computeSize() / t.computeSize();
                                miningResult.ratioNodes = (miningResult.ratioNodes + featureDiffSizeRatio) / 2;
                            });
                        });

                        ++numDiffTrees;
                    }
                }
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
                Logger.error(e, "An unexpected error occurred at {} in {}", commit.getId().getName(), options.repository().getRepositoryName());
                throw e;
            }
        }

        options.analysisStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputPath(), FeatureSplitResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(options.outputPath(), COMMIT_TIME_FILE_EXTENSION));
        return miningResult;
    }
}

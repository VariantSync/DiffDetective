package org.variantsync.diffdetective.analysis;

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
import org.variantsync.diffdetective.util.*;

import java.util.*;

public class FeatureSplitValidationTask extends FeatureSplitAnalysisTask {
    public FeatureSplitValidationTask(FeatureSplitAnalysisTask.Options options) {
        super(options);
    }

    @Override
    public FeatureSplitResult call() throws Exception {
        final FeatureSplitResult miningResult = super.call();
        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();
        final List<CommitProcessTime> commitTimes = new ArrayList<>(HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
        final Clock totalTime = new Clock();
        final Clock patchTime = new Clock();
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
                options.miningStrategy().onCommit(commitDiff, "");
                miningResult.totalPatches += commitDiff.getPatchAmount();

                // inspect every patch
                int numDiffTrees = 0;
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {

                        // generate TreeDiff
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(exportOptions.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!exportOptions.treeFilter().test(t)) {
                            continue;
                        }

                        // calculate sizes of each patch
                        miningResult.initialTreeDiffSizes.put(patch.toString(), t.computeSize());
                        
                        // Add features to results
                        Set<String> allFeatures = FeatureQueryGenerator.featureQueryGenerator(t);
                        miningResult.totalFeatures.addAll(allFeatures);
                        
                        // validate FeatureSplit
                        allFeatures.forEach(feature -> {

                            // calculate time of each input patch
                            patchTime.start();

                            //System.out.println(t.toString());
                            //System.out.println(t.computeSize());

                            // generate feature-aware and remaining patches
                            HashMap<String, DiffTree> featureAware = FeatureSplit.featureSplit(t, PropositionalFormulaParser.Default.parse(feature));
                            
                            System.out.println("FeatureSplit"); 

                            // 1. get number of feature-aware patches for a patch
                            miningResult.ratioOfFAPatches = (miningResult.ratioOfFAPatches + featureAware.size()) / 2;

                            if(featureAware.get(feature) != null) {
                                List<Integer> nodes = miningResult.FAtreeDiffSizes.get(patch.toString());
                                if(nodes == null) nodes = new LinkedList<>();
                                nodes.add(featureAware.get(feature).computeSize());
                                miningResult.FAtreeDiffSizes.put(patch.toString(), nodes);
                            }

                            featureAware.forEach((key, value) -> {
                                if (value == null) return;

                                // 3. check if patch is valid
                                if(!value.isConsistent().isSuccess())  {
                                    Logger.error("incorrectly extracted tree");
                                    ++miningResult.invalidFADiff;
                                }

                                // 4. calculate size of feature-aware difftree and size of initial difftree
                                int featureDiffSizeRatio = value.computeSize() / t.computeSize();
                                miningResult.ratioNodes = (miningResult.ratioNodes + featureDiffSizeRatio) / 2;
                            });

                            // calc patch times
                            List<Long> nodes = miningResult.patchTimes.get(patch.toString());
                            if(nodes == null) nodes = new LinkedList<>();
                            nodes.add(patchTime.getPassedMilliseconds());
                            miningResult.patchTimes.put(patch.toString(), nodes);
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
                Logger.error(e, "An unexpected error occurred at {} in {}", commit.getId().getName(), getOptions().repository().getRepositoryName());
                throw e;
            }
        }

        options.miningStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputPath(), FeatureSplitResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(options.outputPath(), COMMIT_TIME_FILE_EXTENSION));
        return miningResult;
    }
}

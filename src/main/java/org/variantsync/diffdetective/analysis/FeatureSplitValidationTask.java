package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.ConsistencyResult;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.difftree.transform.FeatureSplit;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
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
        totalTime.start();
        final Clock commitProcessTimer = new Clock();
        final Clock patchProcessTimer = new Clock();

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
                // Count elementary patterns
                int numDiffTrees = 0;
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {
                        patchProcessTimer.start();

                        // generate TreeDiffs
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(exportOptions.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!exportOptions.treeFilter().test(t)) {
                            continue;
                        }

                        // validate FeatureSplit
                        Set<String> allFeatures = FeatureQueryGenerator.featureQueryGenerator(t);
                        miningResult.totalFeatures.addAll(allFeatures); // TODO required to calculate run time and feature-aware diffs per feature
                        allFeatures.forEach(feature -> {
                            // "True" would result in an unchanged diff
                            // if(feature.equals("True")) return;
                            LinkedHashMap<String, String> patchStats = new LinkedHashMap<>();
                            HashMap<String, DiffTree> featureAware = FeatureSplit.featureSplit(t, feature);

                            // 1. get number of feature-aware patches for a patch
                            int numOfFeaturesPatches = featureAware.size();
                            if (miningResult.totalFeatureAwarePatches.get(feature) == null) miningResult.totalFeatureAwarePatches.put(feature, 0);
                            miningResult.totalFeatureAwarePatches.replace(feature, miningResult.totalFeatureAwarePatches.get(feature) + numOfFeaturesPatches);
                            
                            patchStats.put(FeatureSplitMetadataKeys.NUM_OF_FEATURE_AWARE_PATCHES, Integer.toString(numOfFeaturesPatches));

                            featureAware.forEach((key, value) -> {
                                if (value == null) return;
                                // 2. get calculation time for a patch
                                final long patchTimeMS = patchProcessTimer.getPassedMilliseconds();
                                patchStats.put(FeatureSplitMetadataKeys.PATCH_TIME_MS, Long.toString(patchTimeMS));

                                // 3. check if patch is valid
                                if(!value.isConsistent().isSuccess())  {
                                    Logger.error("incorrectly extracted tree");
                                    ++miningResult.invalidFADiff;
                                }

                                // 4. check how many feature formulas exists and number of initial features
                                Set<String> features = FeatureQueryGenerator.featureQueryGenerator(value);
                                int numOfFeatures = features.size();
                                patchStats.put(FeatureSplitMetadataKeys.NUM_OF_FEATURES, Integer.toString(numOfFeatures));

                                // 5. calculate size of feature-aware difftree and size of initial difftree
                                int featureDiffSize = value.computeSize();
                                //patchStats.put(FeatureSplitMetadataKeys.FEATURE_AWARE_DIFF_SIZE, Integer.toString(featureDiffSize));
                                patchStats.put(FeatureSplitMetadataKeys.RATIO_OF_NODES, Integer.toString(featureDiffSize / t.computeSize()));

                            });
                            miningResult.putPatchStats(patchStats);
                        });

                        ++numDiffTrees;
                    }
                }
                // TODO Not necessary, create own mining result
                miningResult.exportedTrees += numDiffTrees;

                exportOptions.treeFilter().resetExplanations();

                // Only consider non-empty commits
                // TODO used to generate calc times
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

package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.util.Clock;

public class FeatureSplitFeatureExtractionTask extends FeatureSplitAnalysisTask {
    public FeatureSplitFeatureExtractionTask(FeatureSplitAnalysisTask.Options options) {
        super(options);
    }

    /**
     * Generate a list of all features contained in a repo
     */
    @Override
    public FeatureSplitResult call() throws Exception {
        final FeatureSplitResult miningResult = super.call();
        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();
        final Clock featureExtractTime = new Clock();
        featureExtractTime.start();

        for (final RevCommit commit : options.commits()) {
            try {
                final CommitDiffResult commitDiffResult = options.differ().createCommitDiff(commit);

                if (commitDiffResult.diff().isEmpty()) {
                    Logger.debug("[MiningTask::call] found commit that failed entirely and was not filtered because:\n{}", commitDiffResult.errors());
                    continue;
                }

                final CommitDiff commitDiff = commitDiffResult.diff().get();
                options.miningStrategy().onCommit(commitDiff, "");
                // inspect every patch
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {

                        // generate TreeDiff
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(exportOptions.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!exportOptions.treeFilter().test(t)) {
                            continue;
                        }

                        // Store all occurring features, which are then used to extract feature aware patches and remaining patches
                        miningResult.totalFeatures.addAll(FeatureQueryGenerator.featureQueryGenerator(t));
                    }
                }
                exportOptions.treeFilter().resetExplanations();                
            } catch (Exception e) { 
                Logger.error(e, "An unexpected error occurred at {} in {}", commit.getId().getName(), getOptions().repository().getRepositoryName());
                throw e;
            }
        }

        miningResult.featureExtractTime = featureExtractTime.getPassedSeconds();
        return miningResult;
    }
}

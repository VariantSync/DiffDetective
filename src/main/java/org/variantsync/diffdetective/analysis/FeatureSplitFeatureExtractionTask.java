package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.transform.DiffTreeTransformer;

public class FeatureSplitFeatureExtractionTask extends AnalysisTask<FeatureSplitResult> {
    public FeatureSplitFeatureExtractionTask(Options options) {
        super(options);
    }

    /**
     * Generate a list of all features contained in a repo
     */
    @Override
    public FeatureSplitResult call() throws Exception {
        final var miningResult = new FeatureSplitResult(options.repository().getRepositoryName());
        initializeResult(miningResult);

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
                options.analysisStrategy().onCommit(commitDiff).close();
                // inspect every patch
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {

                        // generate TreeDiff
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(options.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!options.treeFilter().test(t)) {
                            continue;
                        }

                        // Store all occurring features, which are then used to extract feature aware patches and remaining patches
                        miningResult.totalFeatures.addAll(FeatureQueryGenerator.featureQueryGenerator(t));
                    }
                }
                options.treeFilter().resetExplanations();
            } catch (Exception e) {
                Logger.error(e, "An unexpected error occurred at {} in {}", commit.getId().getName(), options.repository().getRepositoryName());
                throw e;
            }
        }

        miningResult.featureExtractTime = featureExtractTime.getPassedSeconds();
        return miningResult;
    }
}

package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.AnalysisTask;
import org.variantsync.diffdetective.analysis.AnalysisTaskFactory;
import org.variantsync.diffdetective.analysis.FACommitExtractionAnalysisTaskFactory;
import org.variantsync.diffdetective.analysis.FACommitExtractionValidationTask;
import org.variantsync.diffdetective.analysis.FeatureSplitFeatureExtractionTask;
import org.variantsync.diffdetective.analysis.FeatureSplitResult;
import org.variantsync.diffdetective.analysis.strategies.NullStrategy;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

public class FACommitValidation {
    public static final FACommitExtractionAnalysisTaskFactory VALIDATION_TASK_FACTORY =
            (repo, differ, outputPath, commits, randomFeatures) -> new FACommitExtractionValidationTask(new AnalysisTask.Options(
                    repo,
                    differ,
                    outputPath,
                    new ExplainedFilter<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    List.of(new CutNonEditedSubtrees()),
                    new NullStrategy(),
                    commits
            ),
            randomFeatures);

    public static final AnalysisTaskFactory<FeatureSplitResult> FEATURE_EXTRACTION_TASK_FACTORY =
            (repo, differ, outputPath, commits) -> new FeatureSplitFeatureExtractionTask(new AnalysisTask.Options(
                    repo,
                    differ,
                    outputPath,
                    new ExplainedFilter<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    List.of(new CutNonEditedSubtrees()),
                    new NullStrategy(),
                    commits
            ));


    public static void main(String[] args) throws IOException {
        Validation.run(args, (repo, repoOutputDir) -> {
            Logger.info(" === Begin Feature Extraction {} ===", repo.getRepositoryName());
            var featureExtrationResult = (FeatureSplitResult)Analysis.forEachCommit(
                repo,
                repoOutputDir,
                FEATURE_EXTRACTION_TASK_FACTORY,
                new FeatureSplitResult(repo.getRepositoryName()),
                1
            );
            Set<String> extractedFeatures = featureExtrationResult.totalFeatures;

            Logger.info(" === Begin Evaluation {} ===", repo.getRepositoryName());

            int numOfFeatures = 3;

            // Select desired features
            Set<String> randomFeatures = new HashSet<>();
            List<String> rndFeatures = new ArrayList<>(extractedFeatures);
            Collections.shuffle(rndFeatures);
            if(rndFeatures.size() >= numOfFeatures) {
                randomFeatures.addAll(rndFeatures.subList(0, numOfFeatures));
            } else {
                randomFeatures.addAll(rndFeatures);
            }

            Analysis.forEachCommit(
                repo,
                repoOutputDir,
                (repository, differ, outputFile, commitList) ->
                    VALIDATION_TASK_FACTORY.create(repository, differ, outputFile, commitList, randomFeatures),
                new FeatureSplitResult(repo.getRepositoryName(), randomFeatures)
            );
        });
    }
}

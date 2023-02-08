package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FeatureExtractionAnalysis;
import org.variantsync.diffdetective.analysis.FeatureSplitResult;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

public class FACommitValidation {
    public static void main(String[] args) throws IOException {
        Validation.run(args, (repo, repoOutputDir) -> {
            Logger.info(" === Begin Feature Extraction {} ===", repo.getRepositoryName());
            var featureExtrationResult =
                Analysis.forEachCommit(
                    () -> new Analysis<>(
                        List.of(
                            new PreprocessingAnalysis<>(new CutNonEditedSubtrees()),
                            new FilterAnalysis<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                            new FeatureExtractionAnalysis()
                        ),
                        repo,
                        repoOutputDir,
                        new FeatureSplitResult()
                    ),
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

            Analysis.forEachCommit(() -> new Analysis<>(
                List.of(
                    new PreprocessingAnalysis<>(new CutNonEditedSubtrees()),
                    new FilterAnalysis<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    new FACommitValidationAnalysis(randomFeatures),
                    new StatisticsAnalysis<>()
                ),
                repo,
                repoOutputDir,
                new FeatureSplitResult(randomFeatures)
            ));
        });
    }
}

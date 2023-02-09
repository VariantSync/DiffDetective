package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.util.List;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FeatureSplitResult;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

public class FeatureSplitValidation {
    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
        Validation.run(args, (repo, repoOutputDir) ->
            Analysis.forEachCommit(() -> new Analysis<>(
                List.of(
                    new PreprocessingAnalysis<>(new CutNonEditedSubtrees()),
                    new FilterAnalysis<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    new FeatureSplitValidationAnalysis(),
                    new StatisticsAnalysis<>()
                ),
                repo,
                repoOutputDir,
                new FeatureSplitResult()
            ))
        );
    }
}

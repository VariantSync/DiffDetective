package org.variantsync.diffdetective.validation;

import java.io.IOException;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.AnalysisTask;
import org.variantsync.diffdetective.analysis.AnalysisTaskFactory;
import org.variantsync.diffdetective.analysis.FeatureSplitResult;
import org.variantsync.diffdetective.analysis.FeatureSplitValidationTask;
import org.variantsync.diffdetective.analysis.strategies.NullStrategy;

public class ValidationFeatureSplit {

    public static final AnalysisTaskFactory<FeatureSplitResult> VALIDATION_TASK_FACTORY =
            (repo, differ, outputPath, commits) -> new FeatureSplitValidationTask(new AnalysisTask.Options(
                    repo,
                    differ,
                    outputPath,
                    Validation.ValidationExportOptions(repo),
                    new NullStrategy(),
                    commits
            ));

    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
        Validation.run(args, (repo, repoOutputDir) ->
            Analysis.forEachCommit(
                repo,
                repoOutputDir,
                VALIDATION_TASK_FACTORY,
                new FeatureSplitResult(repo.getRepositoryName())
        ));
    }
}

package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.util.List;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.AnalysisTask;
import org.variantsync.diffdetective.analysis.AnalysisTaskFactory;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisResult;
import org.variantsync.diffdetective.analysis.strategies.NullStrategy;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.filter.ExplainedFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

/**
 * This is the validation from our ESEC/FSE'22 paper.
 * It provides all configuration settings and facilities to setup the validation by
 * creating a {@link Analysis} and run it.
 * @author Paul Bittner
 */
public class EditClassValidation {
    /**
     * The {@link AnalysisTaskFactory} for the {@link Analysis} that will run our validation.
     * This factory creates {@link EditClassValidationTask}s with the respective settings.
     */
    public static final AnalysisTaskFactory<CommitHistoryAnalysisResult> VALIDATION_TASK_FACTORY =
            (repo, differ, outputPath, commits) -> new EditClassValidationTask(new AnalysisTask.Options(
                    repo,
                    differ,
                    outputPath,
                    new ExplainedFilter<>(DiffTreeFilter.notEmpty()), // filters unwanted trees
                    List.of(new CutNonEditedSubtrees()),
                    new NullStrategy(),
                    commits
            ));

    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
//        setupLogger(Level.INFO);
//        setupLogger(Level.DEBUG);

        Validation.run(args, (repo, repoOutputDir) ->
            Analysis.forEachCommit(
                repo,
                repoOutputDir,
                VALIDATION_TASK_FACTORY,
                new CommitHistoryAnalysisResult(repo.getRepositoryName())
            )
        );
    }
}

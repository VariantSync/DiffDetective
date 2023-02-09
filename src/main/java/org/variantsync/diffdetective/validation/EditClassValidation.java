package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

/**
 * This is the validation from our ESEC/FSE'22 paper.
 * It provides all configuration settings and facilities to setup the validation by
 * creating a {@link Analysis} and run it.
 * @author Paul Bittner
 */
public class EditClassValidation implements Analysis.Hooks {
    // This is only needed for the `MarlinDebug` test.
    public static final BiFunction<Repository, Path, Analysis> AnalysisFactory = (repo, repoOutputDir) -> new Analysis(
        "EditClassValidation",
        List.of(
            new PreprocessingAnalysis(new CutNonEditedSubtrees()),
            new FilterAnalysis(DiffTreeFilter.notEmpty()), // filters unwanted trees
            new EditClassValidation(),
            new StatisticsAnalysis()
        ),
        repo,
        repoOutputDir
    );

    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
//        setupLogger(Level.INFO);
//        setupLogger(Level.DEBUG);

        Validation.run(args, (repo, repoOutputDir) ->
            Analysis.forEachCommit(() -> AnalysisFactory.apply(repo, repoOutputDir))
        );
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(EditClassCount.KEY, new EditClassCount());
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        analysis.getDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                analysis.get(EditClassCount.KEY).reportOccurrenceFor(
                    ProposedEditClasses.Instance.match(node),
                    analysis.getCommitDiff()
                );
            }
        });

        return true;
    }
}

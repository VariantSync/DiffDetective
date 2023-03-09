package org.variantsync.diffdetective.validation;

import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.mining.formats.DirectedEdgeLabelFormat;
import org.variantsync.diffdetective.mining.formats.MiningNodeFormat;
import org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;

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
     * Returns the node format that should be used for DiffNode IO.
     */
    public static MiningNodeFormat NodeFormat() {
        return new ReleaseMiningDiffNodeFormat();
    }

    /**
     * Returns the edge format that should be used for IO of edges in DiffTrees.
     */
    private static EdgeLabelFormat EdgeFormat(final MiningNodeFormat nodeFormat) {
        final EdgeLabelFormat.Direction direction = EdgeLabelFormat.Direction.ParentToChild;
        return new DirectedEdgeLabelFormat(nodeFormat, false, direction);
    }

    /**
     * Creates new export options for running the validation on the given repository.
     */
    public static LineGraphExportOptions ValidationExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new LineGraphExportOptions(
                GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
                , nodeFormat
                , EdgeFormat(nodeFormat)
                , LineGraphExportOptions.LogError()
                .andThen(LineGraphExportOptions.RenderError())
                .andThen(LineGraphExportOptions.SysExitOnError())
        );
    }

    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
        Validation.run(Validation.Options.DEFAULT(args), (repo, repoOutputDir) ->
            Analysis.forEachCommit(() -> AnalysisFactory.apply(repo, repoOutputDir))
        );
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(EditClassCount.KEY, new EditClassCount());
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
        analysis.getCurrentDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                analysis.get(EditClassCount.KEY).reportOccurrenceFor(
                    ProposedEditClasses.Instance.match(node),
                    analysis.getCurrentCommitDiff()
                );
            }
        });

        return true;
    }
}

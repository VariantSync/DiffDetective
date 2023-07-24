package org.variantsync.diffdetective.experiments.esecfse22;

import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.mining.formats.DirectedEdgeLabelFormat;
import org.variantsync.diffdetective.mining.formats.MiningNodeFormat;
import org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.filter.VariationDiffFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat;
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
            new PreprocessingAnalysis(new CutNonEditedSubtrees<>()),
            new FilterAnalysis(VariationDiffFilter.notEmpty()), // filters unwanted trees
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
     * Returns the edge format that should be used for IO of edges in VariationDiffs.
     */
    private static EdgeLabelFormat<DiffLinesLabel> EdgeFormat(final MiningNodeFormat nodeFormat) {
        final EdgeLabelFormat.Direction direction = EdgeLabelFormat.Direction.ParentToChild;
        return new DirectedEdgeLabelFormat(nodeFormat, false, direction);
    }

    /**
     * Creates new export options for running the validation on the given repository.
     */
    public static LineGraphExportOptions<DiffLinesLabel> ValidationExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new LineGraphExportOptions<DiffLinesLabel>(
                GraphFormat.VARIATION_DIFF
                // We have to ensure that all VariationDiffs have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffVariationDiffLabelFormat()
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
        final AnalysisRunner.Options defaultOptions = AnalysisRunner.Options.DEFAULT(args);
        final AnalysisRunner.Options validationOptions = new AnalysisRunner.Options(
                defaultOptions.repositoriesDirectory(),
                defaultOptions.outputDirectory(),
                defaultOptions.datasetsFile(),
                repo -> {
                    final PatchDiffParseOptions defaultPatchDiffParseOptions = defaultOptions.getParseOptionsForRepo().apply(repo);
                    return new PatchDiffParseOptions(
                            defaultPatchDiffParseOptions.diffStoragePolicy(),
                            new VariationDiffParseOptions(
                                    defaultPatchDiffParseOptions.variationDiffParseOptions().annotationParser(),
                                    true,
                                    true
                            )
                    );
                },
                defaultOptions.getFilterForRepo(),
                true,
                false
        );

        AnalysisRunner.run(validationOptions, (repo, repoOutputDir) ->
            Analysis.forEachCommit(() -> AnalysisFactory.apply(repo, repoOutputDir))
        );
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(EditClassCount.KEY, new EditClassCount(ProposedEditClasses.Instance));
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) {
        analysis.getCurrentVariationDiff().forAll(node -> {
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

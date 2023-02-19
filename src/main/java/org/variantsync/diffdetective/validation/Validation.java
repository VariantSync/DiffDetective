package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.tinylog.Logger;

import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.analysis.FilterAnalysis;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.analysis.PreprocessingAnalysis;
import org.variantsync.diffdetective.analysis.StatisticsAnalysis;
import org.variantsync.diffdetective.datasets.*;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.mining.formats.DirectedEdgeLabelFormat;
import org.variantsync.diffdetective.mining.formats.MiningNodeFormat;
import org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.filter.DiffTreeFilter;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;

/**
 * This is the validation from our ESEC/FSE'22 paper.
 * It provides all configuration settings and facilities to setup the validation by
 * creating a {@link Analysis} and run it.
 * @author Paul Bittner
 */
public class Validation implements Analysis.Hooks {
    /**
     * Hardcoded configuration option that determines of all analyzed repositories should be updated
     * (i.e., <code>git pull</code>) before the validation.
     * This should be false and is false by default to make results comparable.
     */
    public static final boolean UPDATE_REPOS_BEFORE_VALIDATION = false;
//    /**
//     * Hardcoded configuration option that determines if
//     */
//    public static final boolean PRINT_LATEX_TABLE = true;
//    public static final int PRINT_LARGEST_SUBJECTS = 3;

    // This is only needed for the `MarlinDebug` test.
    public static final BiFunction<Repository, Path, Analysis> AnalysisFactory = (repo, repoOutputDir) -> new Analysis(
        List.of(
            new PreprocessingAnalysis(new CutNonEditedSubtrees()),
            new FilterAnalysis(DiffTreeFilter.notEmpty()), // filters unwanted trees
            new Validation(),
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

//    public static void printLaTeXTableFor(final List<DatasetDescription> datasets) {
//        Logger.info("Its dangerous outside. Take this!");
//        System.out.println(DatasetDescription.asLaTeXTable(datasets));
//
//        Logger.info("The {} largest systems are:", PRINT_LARGEST_SUBJECTS);
//        final Comparator<DatasetDescription> larger = (a, b) -> {
//            final int ai = Integer.parseInt(a.commits().replaceAll(",", ""));
//            final int bi = Integer.parseInt(b.commits().replaceAll(",", ""));
//            return -Integer.compare(ai, bi);
//        };
//        final List<DatasetDescription> largestDatasets = datasets.stream()
//                .sorted(larger)
//                .limit(PRINT_LARGEST_SUBJECTS)
//                .collect(Collectors.toList());
//        datasets.stream()
//                .filter(m -> m.name().equalsIgnoreCase("Marlin")
//                        || m.name().equalsIgnoreCase("libssh")
//                        || m.name().equalsIgnoreCase("Busybox")
//                        || m.name().equalsIgnoreCase("Godot"))
//                .forEach(largestDatasets::add);
//        largestDatasets.sort(larger);
//        System.out.println(DatasetDescription.asLaTeXTable(largestDatasets));
//    }

    /**
     * Main method to start the validation.
     * @param args Command-line options. Currently ignored.
     * @throws IOException When copying the log file fails.
     */
    public static void main(String[] args) throws IOException {
//        setupLogger(Level.INFO);
//        setupLogger(Level.DEBUG);

        final Path datasetsFile;
        if (args.length < 1) {
            datasetsFile = DefaultDatasets.DEFAULT_DATASETS_FILE;
        } else if (args.length > 1) {
            Logger.error("Error: Expected exactly one argument but got " + args.length + "! Expected a path to a datasets markdown file.");
            return;
        } else {
            datasetsFile = Path.of(args[0]);

            if (!Files.exists(datasetsFile)) {
                Logger.error("The given datasets file \"" + datasetsFile + "\" does not exist.");
            }
        }

        final ParseOptions.DiffStoragePolicy diffStoragePolicy = ParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER;

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        Logger.info("Reading and cloning git repositories from/to: " + inputDir);

        final Path outputDir = Paths.get("results", "validation", "current");
        Logger.info("Writing output to: " + outputDir);

        Logger.info("Loading datasets file: " + datasetsFile);
        final List<Repository> repos;
        final List<DatasetDescription> datasets = DefaultDatasets.loadDatasets(datasetsFile);

//        if (PRINT_LATEX_TABLE) {
//            printLaTeXTableFor(datasets);
//        }

        final DatasetFactory miningDatasetFactory = new DatasetFactory(inputDir);
        repos = datasets.stream().map(miningDatasetFactory::create).collect(Collectors.toList());

        Logger.info("Performing validation on the following repositories:");
        for (final Repository repo : repos) {
            repo.setParseOptions(repo.getParseOptions().withDiffStoragePolicy(diffStoragePolicy));
            Logger.info("  - {} from {}", repo.getRepositoryName(), repo.getRemoteURI());
        }

        Logger.info("Preloading repositories:");
        for (final Repository repo : repos) {
            repo.getGitRepo().run();
        }

        if (UPDATE_REPOS_BEFORE_VALIDATION) {
            Logger.info("Updating repositories:");
            for (final Repository repo : repos) {
                try {
                    Assert.assertTrue(repo.getGitRepo().run().pull().call().isSuccessful());
                } catch (GitAPIException e) {
                    Logger.error(e, "Failed to pull repository '{}'", repo.getRepositoryName());
                }
            }
        }

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        Analysis.forEachRepository(repos, outputDir, (repo, repoOutputDir) ->
            Analysis.forEachCommit(() -> AnalysisFactory.apply(repo, repoOutputDir))
        );
        Logger.info("Done");

        final String logFile = "log.txt";
        FileUtils.copyFile(Path.of(logFile).toFile(), outputDir.resolve(logFile).toFile());
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        analysis.getCurrentDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                analysis.getResult().editClassCounts.reportOccurrenceFor(
                    ProposedEditClasses.Instance.match(node),
                    analysis.getCurrentCommitDiff()
                );
            }
        });

        return true;
    }
}

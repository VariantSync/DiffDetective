package org.variantsync.diffdetective.relationshipedges;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTaskFactory;
import org.variantsync.diffdetective.analysis.HistoryAnalysis;
import org.variantsync.diffdetective.analysis.strategies.AnalyzeAllThenExport;
import org.variantsync.diffdetective.datasets.*;
import org.variantsync.diffdetective.diff.difftree.filter.DiffTreeFilter;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.mining.formats.DirectedEdgeLabelFormat;
import org.variantsync.diffdetective.mining.formats.MiningNodeFormat;
import org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat;
import org.variantsync.diffdetective.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This is the validation for the paper.
 * It provides all configuration settings and facilities to setup the validation by
 * creating a {@link HistoryAnalysis} and run it.
 */
public class Validation {
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

    /**
     * The {@link CommitHistoryAnalysisTaskFactory} for the {@link HistoryAnalysis} that will run our validation.
     * This factory creates {@link PaperEvaluationTask}s with the respective settings.
     */
    public static final CommitHistoryAnalysisTaskFactory VALIDATION_TASK_FACTORY =
            (repo, differ, outputPath, commits) -> new PaperEvaluationTask(new CommitHistoryAnalysisTask.Options(
                    repo,
                    differ,
                    outputPath,
                    new ExplainedFilter<>(DiffTreeFilter.notEmpty()),
                    List.of(new CutNonEditedSubtrees()),
                    new AnalyzeAllThenExport(), // new NullStrategy(),
                    commits
            ));

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
            datasetsFile = DefaultDatasets.TESTING_DATASETS_FILE;
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

        final Path outputDir = Paths.get("results", "relationShipEdges", "current");
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

        final Consumer<Path> repoPostProcessing = p -> {};
        final HistoryAnalysis analysis = new HistoryAnalysis(
                repos,
                outputDir,
                HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT,
                VALIDATION_TASK_FACTORY,
                repoPostProcessing);
        analysis.runAsync();
        Logger.info("Done");

        final String logFile = "log.txt";
        FileUtils.copyFile(Path.of(logFile).toFile(), outputDir.resolve(logFile).toFile());
    }
}

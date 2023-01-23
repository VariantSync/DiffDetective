package org.variantsync.diffdetective.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.DatasetDescription;
import org.variantsync.diffdetective.datasets.DatasetFactory;
import org.variantsync.diffdetective.datasets.DefaultDatasets;
import org.variantsync.diffdetective.datasets.ParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.difftree.filter.DiffTreeFilter;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.mining.formats.DirectedEdgeLabelFormat;
import org.variantsync.diffdetective.mining.formats.MiningNodeFormat;
import org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat;
import org.variantsync.diffdetective.util.Assert;

public class Validation {
    private Validation() {
    }

    /**
     * Hardcoded configuration option that determines of all analyzed repositories should be updated
     * (i.e., <code>git pull</code>) before the validation.
     * This should be false and is false by default to make results comparable.
     */
    public static final boolean UPDATE_REPOS_BEFORE_VALIDATION = false;
    public static final boolean PRINT_LATEX_TABLE = false;
    public static final int PRINT_LARGEST_SUBJECTS = 3;

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
    public static DiffTreeLineGraphExportOptions ValidationExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
                , nodeFormat
                , EdgeFormat(nodeFormat)
                , new ExplainedFilter<>(DiffTreeFilter.notEmpty()) // filters unwanted trees
                , List.of(new CutNonEditedSubtrees())
                , DiffTreeLineGraphExportOptions.LogError()
                .andThen(DiffTreeLineGraphExportOptions.RenderError())
                .andThen(DiffTreeLineGraphExportOptions.SysExitOnError())
        );
    }

    public static void printLaTeXTableFor(final List<DatasetDescription> datasets) {
        Logger.info("Its dangerous outside. Take this!");
        System.out.println(DatasetDescription.asLaTeXTable(datasets));

        Logger.info("The {} largest systems are:", PRINT_LARGEST_SUBJECTS);
        final Comparator<DatasetDescription> larger = (a, b) -> {
            final int ai = Integer.parseInt(a.commits().replaceAll(",", ""));
            final int bi = Integer.parseInt(b.commits().replaceAll(",", ""));
            return -Integer.compare(ai, bi);
        };
        final List<DatasetDescription> largestDatasets = datasets.stream()
                .sorted(larger)
                .limit(PRINT_LARGEST_SUBJECTS)
                .collect(Collectors.toList());
        datasets.stream()
                .filter(m -> m.name().equalsIgnoreCase("Marlin")
                        || m.name().equalsIgnoreCase("libssh")
                        || m.name().equalsIgnoreCase("Busybox")
                        || m.name().equalsIgnoreCase("Godot"))
                .forEach(largestDatasets::add);
        largestDatasets.sort(larger);
        System.out.println(DatasetDescription.asLaTeXTable(largestDatasets));
    }

    /**
     * Main method to start the validation.
     * @param args Command-line options.
     * @throws IOException When copying the log file fails.
     */
    public static void run(String[] args, BiConsumer<Repository, Path> validation) throws IOException {
        final Path datasetsFile;
        if (args.length < 1) {
            datasetsFile = DefaultDatasets.DEFAULT_DATASETS_FILE;
        } else if (args.length > 1) {
            Logger.error("Error: Expected exactly one argument but got " + args.length + "! Expected a path to a datasets markdown file.");
            return;
        } else {
            datasetsFile = Path.of(args[0]);
        }

        if (!Files.exists(datasetsFile)) {
            Logger.error("The given datasets file \"" + datasetsFile + "\" does not exist.");
        }

        final ParseOptions.DiffStoragePolicy diffStoragePolicy = ParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER;

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path outputDir = Paths.get("results", "validation", "current");
        Logger.info("Writing output to: " + outputDir);

        Logger.info("Loading datasets file: " + datasetsFile);
        final List<Repository> repos;
        final List<DatasetDescription> datasets = DefaultDatasets.loadDatasets(datasetsFile);

        if (PRINT_LATEX_TABLE) {
            printLaTeXTableFor(datasets);
        }

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
            validation.accept(repo, repoOutputDir)
        );
        Logger.info("Done");

        final String logFile = "log.txt";
        FileUtils.copyFile(Path.of(logFile).toFile(), outputDir.resolve(logFile).toFile());
    }
}

package validation;

import analysis.CommitHistoryAnalysisTask;
import analysis.CommitHistoryAnalysisTaskFactory;
import analysis.HistoryAnalysis;
import analysis.strategies.NullStrategy;
import datasets.*;
import diff.difftree.filter.DiffTreeFilter;
import diff.difftree.filter.ExplainedFilter;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.difftree.transform.CutNonEditedSubtrees;
import mining.formats.DirectedEdgeLabelFormat;
import mining.formats.MiningNodeFormat;
import mining.formats.ReleaseMiningDiffNodeFormat;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.tinylog.Logger;
import util.Assert;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Validation {
    public static final boolean UPDATE_REPOS_BEFORE_VALIDATION = false;
    public static final boolean PRINT_LATEX_TABLE = true;
    public static final int PRINT_LARGEST_SUBJECTS = 3;

    public static final CommitHistoryAnalysisTaskFactory VALIDATION_TASK_FACTORY =
            (repo, differ, outputPath, commits) -> new PatternValidationTask(new CommitHistoryAnalysisTask.Options(
                    repo,
                    differ,
                    outputPath,
                    ValidationExportOptions(repo),
                    new NullStrategy(),
                    commits
            ));

    public static MiningNodeFormat NodeFormat() {
        return new ReleaseMiningDiffNodeFormat();
    }

    private static EdgeLabelFormat EdgeFormat(final MiningNodeFormat nodeFormat) {
        final EdgeLabelFormat.Direction direction = EdgeLabelFormat.Direction.ParentToChild;
        return new DirectedEdgeLabelFormat(nodeFormat, false, direction);
    }

    public static DiffTreeLineGraphExportOptions ValidationExportOptions(final Repository repository) {
        final MiningNodeFormat nodeFormat = NodeFormat();
        return new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE
                // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                , new CommitDiffDiffTreeLabelFormat()
                , nodeFormat
                , EdgeFormat(nodeFormat)
                , new ExplainedFilter<>(DiffTreeFilter.notEmpty())
                , List.of(new CutNonEditedSubtrees())
                , DiffTreeLineGraphExportOptions.LogError()
                .andThen(DiffTreeLineGraphExportOptions.RenderError())
                .andThen(DiffTreeLineGraphExportOptions.SysExitOnError())
        );
    }

    public static void printLaTeXTableFor(final List<DatasetDescription> datasets) {
        Logger.info("Its dangerous outside. Take this!");
        System.out.println(DatasetDescription.asLaTeXTable(datasets));

        Logger.info("The " + PRINT_LARGEST_SUBJECTS + " largest systems are:");
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

    public static void main(String[] args) throws IOException {
//        setupLogger(Level.INFO);
//        setupLogger(Level.DEBUG);

        final ParseOptions.DiffStoragePolicy diffStoragePolicy = ParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER;

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path outputDir = Paths.get("results", "difftrees");

        final List<Repository> repos;
        final List<DatasetDescription> datasets = DefaultDatasets.loadDefaultDatasets();

        if (PRINT_LATEX_TABLE) {
            printLaTeXTableFor(datasets);
        }

        final DatasetFactory miningDatasetFactory = new DatasetFactory(inputDir);
        repos = datasets.stream().map(miningDatasetFactory::create).collect(Collectors.toList());

        Logger.info("Performing validation on the following repositories:");
        for (final Repository repo : repos) {
            repo.setParseOptions(repo.getParseOptions().withDiffStoragePolicy(diffStoragePolicy));
            Logger.info("  - " + repo.getRepositoryName() + " from " + repo.getRemoteURI());
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
                    Logger.error("Failed to pull repository \"" + repo.getRepositoryName() + "\"!", e);
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

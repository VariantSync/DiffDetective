package org.variantsync.diffdetective.validation;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.*;
import org.variantsync.diffdetective.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Main entry point for performing large-scale empirical analyses on a range of git repositories.
 *
 * @author Paul Bittner, Benjamin Moosherr
 */
public class AnalysisRunner {
    /**
     *
     * @param repositoriesDirectory The directory to which all git repositories will be cloned to.
     *                              If this directory already contains the required git repositories,
     *                              those copies will be used.
     * @param outputDirectory The directory to which any output data of the analysis should be written to.
     * @param datasetsFile Path to a markdown file containing a table of git repositories to analyze.
     * @param diffStoragePolicy Policy that decides whether and how the text-based diffs from git should
     *                          be stored in any parsed DiffTrees during the analyses. The default is
     *                          {@link ParseOptions.DiffStoragePolicy#DO_NOT_REMEMBER} as storing all
     *                          the text-diffs as strings can cause a big memory footprint that can be
     *                          avoided when the text-diffs are not used anyway.
     * @param preloadReposBeforeAnalysis Decides whether all repositories should be cloned once before the analysis.
     *                                   If a repository does not have a local clone in {@link #repositoriesDirectory}
     *                                   it will be cloned before the analysis if preloadReposBeforeAnalysis is set to
     *                                   true. Otherwise it will be cloned right before it is about to be analysed.
     * @param pullRepositoriesBeforeAnalysis Decides whether git pull should be run on each repository before analysis.
     *                                       Takes effect only if {@link #preloadReposBeforeAnalysis} is true.
     *                                       Does nothing otherwise.
     */
    public record Options(
            Path repositoriesDirectory,
            Path outputDirectory,
            Path datasetsFile,
            ParseOptions.DiffStoragePolicy diffStoragePolicy,
            boolean preloadReposBeforeAnalysis,
            /* Determines whether all analyzed repositories should be updated
             * (i.e., <code>git pull</code>) before the analysis.
             * This should be false and is false by default to make results comparable.
             */
            boolean pullRepositoriesBeforeAnalysis
    ) {
        public static Options DEFAULT(final String[] args) {
            final Path datasetsFile;
            if (args.length < 1) {
                datasetsFile = DefaultDatasets.DEFAULT_DATASETS_FILE;
            } else if (args.length > 1) {
                throw new IllegalArgumentException("Error: Expected exactly one argument but got " + args.length + "! Expected a path to a datasets markdown file.");
            } else {
                datasetsFile = Path.of(args[0]);
            }

            return new Options(
                    Paths.get("..", "DiffDetectiveMining"),
                    Paths.get("results", "validation", "current"),
                    datasetsFile,
                    ParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER,
                    true,
                    false
            );
        }
    }

    private AnalysisRunner() {
    }

    /**
     * Main method to start the analysis on a set of git repositories.
     * @param options Options that configure the datasets IO process.
     * @throws IOException When copying the log file to the output directory fails.
     */
    public static void run(Options options, BiConsumer<Repository, Path> validation) throws IOException {
        if (!Files.exists(options.datasetsFile())) {
            Logger.error("The given datasets file \"" + options.datasetsFile() + "\" does not exist.");
        }

        final Path inputDir = options.repositoriesDirectory();
        Logger.info("Reading and cloning git repositories from/to: " + inputDir);

        final Path outputDir = options.outputDirectory();
        Logger.info("Writing output to: " + outputDir);

        Logger.info("Loading datasets file: " + options.datasetsFile());
        final List<Repository> repos;
        final List<DatasetDescription> datasets = DefaultDatasets.loadDatasets(options.datasetsFile());

        final DatasetFactory miningDatasetFactory = new DatasetFactory(inputDir);
        repos = datasets.stream().map(miningDatasetFactory::create).collect(Collectors.toList());

        Logger.info("Performing validation on the following repositories:");
        for (final Repository repo : repos) {
            repo.setParseOptions(repo.getParseOptions().withDiffStoragePolicy(options.diffStoragePolicy()));
            Logger.info("  - {} from {}", repo.getRepositoryName(), repo.getRemoteURI());
        }

        if (options.preloadReposBeforeAnalysis()) {
            Logger.info("Preloading repositories:");
            for (final Repository repo : repos) {
                repo.getGitRepo().run();
            }

            if (options.pullRepositoriesBeforeAnalysis()) {
                Logger.info("Updating repositories:");
                for (final Repository repo : repos) {
                    try {
                        Assert.assertTrue(repo.getGitRepo().run().pull().call().isSuccessful());
                    } catch (GitAPIException e) {
                        Logger.error(e, "Failed to pull repository '{}'", repo.getRepositoryName());
                    }
                }
            }
        }

        Analysis.forEachRepository(repos, outputDir, validation);
        Logger.info("Done");

        final String logFile = "log.txt";
        FileUtils.copyFile(Path.of(logFile).toFile(), outputDir.resolve(logFile).toFile());
    }
}

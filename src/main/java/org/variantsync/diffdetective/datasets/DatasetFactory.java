package org.variantsync.diffdetective.datasets;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.predefined.Marlin;
import org.variantsync.diffdetective.datasets.predefined.StanciulescuMarlin;
import org.variantsync.diffdetective.diff.DiffFilter;
import org.variantsync.diffdetective.util.Assert;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * The DatasetFactory loads datasets and provides default values for DiffFilters and parse options.
 * In particular, this class turns {@link DatasetDescription} objects into {@link Repository} objects.
 * @author Paul Bittner
 */
public class DatasetFactory {
    /**
     * Name of Marlin.
     */
    public static final String MARLIN = "Marlin";

    /**
     * Name of Linux.
     */
    public static final String LINUX = "Linux";

    /**
     * Name of PHP.
     */
    public static final String PHP = "PHP";

    /**
     * Default value for diff filters.
     * It disallows merge commits, only considers patches that modified files,
     * and only allows source files of C/C++ projects ("h", "hpp", "c", "cpp").
     */
    public static final DiffFilter DEFAULT_DIFF_FILTER =
            new DiffFilter.Builder()
                    .allowMerge(false)
                    .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
                    .allowedFileExtensions("h", "hpp", "c", "cpp")
                    .build();
//    public static final DiffFilter PHP_DIFF_FILTER =
//            new DiffFilter.Builder(DEFAULT_DIFF_FILTER)
////                    .blockedPaths("ext/fileinfo/data_file.c")
//                    .build();

    private final Path cloneDirectory;

    /**
     * Creates a new DatasetFactory that will clone any loaded datasets to the given directy.
     * @param cloneDirectory Directory to clone remote repositories to upon dataset loading.
     */
    public DatasetFactory(Path cloneDirectory) {
        this.cloneDirectory = cloneDirectory;
    }

    /**
     * Returns the default DiffFilter for the repository with the given name.
     * For Marlin, this applies the same DiffFilter as Stanciulescu et al. did in their ICSME paper.
     * @see StanciulescuMarlin#DIFF_FILTER
     */
    private static DiffFilter getDiffFilterFor(final String repositoryName) {
        if (repositoryName.equalsIgnoreCase(MARLIN)) {
            return StanciulescuMarlin.DIFF_FILTER;
        }
//        if (repositoryName.equalsIgnoreCase(PHP)) {
//            return PHP_DIFF_FILTER;
//        }
        return DEFAULT_DIFF_FILTER;
    }

    /**
     * Returns the default parse options for the repository with the given name.
     * For Marlin, uses the {@link Marlin#ANNOTATION_PARSER}.
     */
    private static ParseOptions getParseOptionsFor(final String repositoryName) {
        if (repositoryName.equalsIgnoreCase(MARLIN)) {
            return new ParseOptions(Marlin.ANNOTATION_PARSER);
        }
        return ParseOptions.Default;
    }

    /**
     * Loads the repository of the given dataset description.
     * This will laod the repository with the DiffFilter and ParseOptions provided by
     * {@link DatasetFactory#getDiffFilterFor} and {@link DatasetFactory#getParseOptionsFor}, respectively.
     * @param dataset The dataset to load.
     * @return A repository referencing the loaded dataset.
     */
    public Repository create(final DatasetDescription dataset) {
        final DiffFilter diffFilter = getDiffFilterFor(dataset.name());
        final ParseOptions parseOptions = getParseOptionsFor(dataset.name());

        final Repository repo = Repository.tryFromRemote(
                cloneDirectory,
                dataset.repoURL(),
                dataset.name())
                .orElseThrow();

        repo.setDiffFilter(diffFilter).setParseOptions(parseOptions);

        return repo;
    }

    /**
     * Runs {@link DatasetFactory#create} for all given dataset description.
     * Optionally, may also preload the repository which means that the repository will be cloned if it is remote or unzipped if it is a zip archive.
     * Optionally, may also run <code>git pull</code> on all repositories to update them.
     * @see org.variantsync.diffdetective.load.GitLoader
     * @param datasets Datasets to load.
     * @param preload Set to true iff the repositories should be cloned / unzipped in case they are not locally available already.
     * @param pull Set to true iff <code>git pull</code> should be run on all repositories before returning.
     * @return Repository references for all dataset descriptions in the same order.
     */
    public List<Repository> createAll(final Collection<DatasetDescription> datasets, boolean preload, boolean pull) {
        final List<Repository> repos = datasets.stream().map(this::create).toList();

        if (preload) {
            Logger.info("Preloading repositories:");
            for (final Repository repo : repos) {
                repo.getGitRepo().run();
            }
        }

        if (pull) {
            Logger.info("Pulling repositories:");
            for (final Repository repo : repos) {
                try {
                    Assert.assertTrue(repo.getGitRepo().run().pull().call().isSuccessful());
                } catch (GitAPIException e) {
                    Logger.error(e, "Failed to pull repository '{}'", repo.getRepositoryName());
                }
            }
        }

        return repos;
    }
}

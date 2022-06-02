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

public class DatasetFactory {
    public static final String MARLIN = "Marlin";
    public static final String LINUX = "Linux";
    public static final String PHP = "PHP";
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

    public DatasetFactory(Path cloneDirectory) {
        this.cloneDirectory = cloneDirectory;
    }

    private static DiffFilter getDiffFilterFor(final String repositoryName) {
        if (repositoryName.equalsIgnoreCase(MARLIN)) {
            return StanciulescuMarlin.DIFF_FILTER;
        }
//        if (repositoryName.equalsIgnoreCase(PHP)) {
//            return PHP_DIFF_FILTER;
//        }
        return DEFAULT_DIFF_FILTER;
    }

    private static ParseOptions getParseOptionsFor(final String repositoryName) {
        if (repositoryName.equalsIgnoreCase(MARLIN)) {
            return new ParseOptions(Marlin.ANNOTATION_PARSER);
        }
        return ParseOptions.Default;
    }

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

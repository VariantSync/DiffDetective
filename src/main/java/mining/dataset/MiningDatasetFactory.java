package mining.dataset;

import datasets.ParseOptions;
import datasets.Repository;
import datasets.predefined.LinuxKernel;
import datasets.predefined.Marlin;
import datasets.predefined.StanciulescuMarlin;
import diff.DiffFilter;
import diff.difftree.DiffNode;
import org.eclipse.jgit.diff.DiffEntry;

import java.nio.file.Path;
import java.util.function.Predicate;

public class MiningDatasetFactory {
    public static final String MARLIN = "Marlin";
    public static final String LINUX = "Linux";
    public static final DiffFilter DEFAULT_DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("h", "hpp", "c", "cpp")
            .build();

    private final Path cloneDirectory;

    public MiningDatasetFactory(Path cloneDirectory) {
        this.cloneDirectory = cloneDirectory;
    }

    private static DiffFilter getDiffFilterFor(final String repositoryName) {
        if (repositoryName.equalsIgnoreCase(MARLIN)) {
            return StanciulescuMarlin.DIFF_FILTER;
        }
        return DEFAULT_DIFF_FILTER;
    }

    private static ParseOptions getParseOptionsFor(final String repositoryName) {
        if (repositoryName.equalsIgnoreCase(MARLIN)) {
            return new ParseOptions(Marlin.ANNOTATION_PARSER);
        }
        return ParseOptions.Default;
    }

    public Repository create(final MiningDataset dataset) {
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
}

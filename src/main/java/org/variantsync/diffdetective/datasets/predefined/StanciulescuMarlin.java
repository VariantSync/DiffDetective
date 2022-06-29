package org.variantsync.diffdetective.datasets.predefined;

import org.eclipse.jgit.diff.DiffEntry;
import org.variantsync.diffdetective.datasets.ParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.DiffFilter;

import java.nio.file.Path;

/**
 * Default repository for the snippet from the Marlin history used in the paper
 * "Concepts, Operations, and Feasibility of a Projection-Based Variation Control System",
 * Stefan Stanciulescu, Thorsten Berger, Eric Walkingshaw, Andrzej Wasowski
 * at ICSME 2016.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class StanciulescuMarlin {
    /**
     * The diff filter that was applied by Stanciulescu et al. in their study.
     * We reproduced their description to the best of our knowledge.
     */
    public static final DiffFilter DIFF_FILTER = new DiffFilter.Builder()
            .allowMerge(false)
            .allowedPaths("Marlin.*")
            .blockedPaths(".*arduino.*")
            .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
            .allowedFileExtensions("c", "cpp", "h", "pde")
            .build();

    /**
     * Instance for the default predefined Marlin repository.
     * @return Marlin repository
     */
    public static Repository fromZipInDiffDetectiveAt(Path pathToDiffDetective) {
        final Path marlinPath = pathToDiffDetective
                .resolve(Repository.DIFFDETECTIVE_DEFAULT_REPOSITORIES_DIRECTORY)
                .resolve("Marlin_old.zip");
        return Repository
                .fromZip(marlinPath, "Marlin_old")
                .setDiffFilter(DIFF_FILTER)
                .setParseOptions(new ParseOptions(Marlin.ANNOTATION_PARSER));
    }
}

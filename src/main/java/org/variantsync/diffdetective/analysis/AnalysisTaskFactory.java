package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.GitDiffer;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Factory for tasks for {@link Analysis}.
 * This factory creates a task to run for a given repository and a given set of commits.
 * @author Paul Bittner
 */
@FunctionalInterface
public interface AnalysisTaskFactory<T> {
    /**
     * Create a task for the given set of commits from the given repository.
     * @param repository The repository for whose analysis a task should be created.
     * @param differ The differ that should be used to create diffs from the given commits.
     * @param outputPath The output path to which any results should be written on disk if necessary.
     * @param commits The set of commits that should be processed by the produced task.
     * @return A task that process the given set of commits.
     */
    Callable<T> create(
        final Repository repository,
        final GitDiffer differ,
        final Path outputPath,
        Iterable<RevCommit> commits
    );
}

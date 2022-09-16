package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;

import java.nio.file.Path;
@FunctionalInterface
public interface FeatureSplitAnalysisTaskFactory {

    FeatureSplitAnalysisTask create(
            final Repository repository,
            final GitDiffer differ,
            final Path outputPath,
            Iterable<RevCommit> commits
            );
}
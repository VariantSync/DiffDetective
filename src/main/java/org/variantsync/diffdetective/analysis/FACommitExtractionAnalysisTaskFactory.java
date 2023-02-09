package org.variantsync.diffdetective.analysis;

import org.eclipse.jgit.revwalk.RevCommit;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.GitDiffer;

import java.nio.file.Path;
import java.util.Set;

@FunctionalInterface
public interface FACommitExtractionAnalysisTaskFactory {

    AnalysisTask<FeatureSplitResult> create(
            final Repository repository,
            final GitDiffer differ,
            final Path outputPath,
            Iterable<RevCommit> commits,
            Set<String> randomFeatures
            );
}

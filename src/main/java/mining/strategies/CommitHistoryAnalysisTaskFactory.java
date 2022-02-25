package mining.strategies;

import datasets.Repository;
import diff.GitDiffer;
import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.Path;

@FunctionalInterface
public interface CommitHistoryAnalysisTaskFactory {
    CommitHistoryAnalysisTask create(
            final Repository repository,
            final GitDiffer differ,
            final Path outputPath,
            Iterable<RevCommit> commits
            );
}

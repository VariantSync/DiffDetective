import datasets.Repository;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.PatchDiff;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Path;

public final class Constants {
    public static final Path RESOURCE_DIR = Path.of("src", "test", "resources");

    static CommitDiff parseCommit(Repository repo, String commitHash) throws IOException {
        final Git git = repo.getGitRepo().run();
        Assert.assertNotNull(git);
        final RevWalk revWalk = new RevWalk(git.getRepository());
        final RevCommit childCommit = revWalk.parseCommit(ObjectId.fromString(commitHash));
        final RevCommit parentCommit = revWalk.parseCommit(childCommit.getParent(0).getId());

        final CommitDiff commitDiff =
                GitDiffer.createCommitDiff(
                                git,
                                repo.getDiffFilter(),
                                parentCommit,
                                childCommit,
                                repo.getParseOptions())
                        .unwrap().first().orElseThrow();

        revWalk.close();
        return commitDiff;
    }

    static PatchDiff parsePatch(Repository repo, String file, String commitHash) throws IOException {
        final CommitDiff commitDiff = parseCommit(repo, commitHash);

        for (final PatchDiff pd : commitDiff.getPatchDiffs()) {
            if (file.equals(pd.getFileName())) {
                return pd;
            }
        }

        Assert.fail("Did not find file \"" + file + "\" in commit " + commitHash + "!");
        return null;
    }
}

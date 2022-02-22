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
}

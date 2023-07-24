import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.DiffFilter;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.FullNodeFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class GitDifferTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("diffs").resolve("differ");
    private final static Path REPOS_DIR = Constants.RESOURCE_DIR.resolve("repos");

    private static final String[] commitIDs = new String[] {"a032346092e47048becb36a7cb183b4739547370", "4a4c1db1b192e221d8e25460d6d1128d1bdd0c0d", "d7eeba1b94f3d0c0ebed3457eea8fa8537143348"};

    public static Stream<String> testCommits() {
        return Arrays.stream(commitIDs);
    }

    @ParameterizedTest
    @MethodSource("testCommits")
    public void test(String commitHash) throws IOException, DiffParseException {
        Repository repo = repo();
        final CommitDiff commitDiff = DiffTreeParser.parseCommit(repo, commitHash);

        for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
            Assertions.assertTrue(patch.isValid());
            var actualPath = testDir.resolve(commitHash + "_actual.lg");
            var expectedPath = testDir.resolve(commitHash + ".lg");

            DiffTree<DiffLinesLabel> diffTree = patch.getDiffTree();

            try (var output = IO.newBufferedOutputStream(actualPath)) {
                new LineGraphExporter<>(new Format<>(new FullNodeFormat(), new ChildOrderEdgeFormat<>()))
                        .exportDiffTree(diffTree, output);
            }

            try (
                    var expectedFile = Files.newBufferedReader(expectedPath);
                    var actualFile = Files.newBufferedReader(actualPath);
            ) {
                if (!IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                    fail("The DiffTree for commit " + commitHash + " didn't parse correctly. "
                            + "Expected the content of " + expectedPath + " but got the content of " + actualPath + ". ");
                    // Keep output files if the test failed
                } else {
                    // Delete output files if the test succeeded
                    Files.delete(actualPath);
                }
            }
        }
    }

    private static Repository repo() {
        final Path repoPath = REPOS_DIR.resolve("test-spl.zip");
        return Repository
                .fromZip(repoPath, "test-spl")
                .setDiffFilter(DiffFilter.ALLOW_ALL)
                .setParseOptions(new PatchDiffParseOptions(PatchDiffParseOptions.DiffStoragePolicy.REMEMBER_FULL_DIFF, new DiffTreeParseOptions(
                        false,
                        false
                )));
    }

}

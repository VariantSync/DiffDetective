import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.datasets.predefined.Marlin;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.DiffFilter;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.mining.DiffTreeMiner;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExporter;
import org.variantsync.diffdetective.variation.diff.serialize.TikzExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.FullNodeFormat;
import org.variantsync.diffdetective.variation.diff.transform.DiffTreeTransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

public class GitDifferTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("diffs").resolve("differ");
    private final static Path REPOS_DIR = Path.of("src").resolve("test").resolve("resources").resolve("repos");

    private static final String[] commitIDs = new String[] {"4a4c1db1b192e221d8e25460d6d1128d1bdd0c0d", "d7eeba1b94f3d0c0ebed3457eea8fa8537143348"};

    public static Stream<String> testCommits() throws IOException {
        return Arrays.stream(commitIDs);
    }

    @ParameterizedTest
    @MethodSource("testCommits")
    public void test(String basename) throws IOException, DiffParseException {
        testCase(basename);
    }

    private static Repository repo() {
        final Path marlinPath = Path.of(".")
                .resolve(REPOS_DIR)
                .resolve("test-spl.zip");
        return Repository
                .fromZip(marlinPath, "test-spl")
                .setDiffFilter(DiffFilter.ALLOW_ALL)
                .setParseOptions(new PatchDiffParseOptions(PatchDiffParseOptions.DiffStoragePolicy.REMEMBER_FULL_DIFF, new DiffTreeParseOptions(
                        false,
                        false
                )));
    }

    public void testCase(String commitHash) throws IOException, DiffParseException {
        Repository repo = repo();
        final CommitDiff commitDiff = DiffTreeParser.parseCommit(repo, commitHash);

        for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
            Assertions.assertTrue(patch.isValid());
            var actualPath = testDir.resolve(commitHash + "_actual.lg");
            var expectedPath = testDir.resolve(commitHash + ".lg");

            DiffTree diffTree = patch.getDiffTree();

            try (var output = IO.newBufferedOutputStream(actualPath)) {
                new LineGraphExporter(new Format(new FullNodeFormat(), new ChildOrderEdgeFormat()))
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
}

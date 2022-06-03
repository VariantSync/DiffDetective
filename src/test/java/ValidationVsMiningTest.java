import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.*;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.filter.DiffTreeFilter;
import org.variantsync.diffdetective.diff.difftree.filter.ExplainedFilter;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.render.RenderOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.mining.DiffTreeMiner;
import org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat;
import org.variantsync.diffdetective.util.FullyQualifiedPatch;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.validation.Validation;
import org.variantsync.functjonal.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ValidationVsMiningTest {
    private static final Path reposPath = Paths.get("..", "DiffDetectiveMining");
    private static List<FullyQualifiedPatch> FISHY_COMMITS;
    private static final Path OUTPUT_PATH = Path.of("debug", "vvsm");

    @BeforeClass
    public static void init() {
        final DatasetDescription emacsDesc = DefaultDatasets.loadDatasets(DefaultDatasets.EMACS).get(0);
        final Repository emacs = new DatasetFactory(reposPath).create(emacsDesc);
        emacs.setParseOptions(emacs.getParseOptions().withDiffStoragePolicy(ParseOptions.DiffStoragePolicy.REMEMBER_FULL_DIFF));

        FISHY_COMMITS = Stream.of(
                new Pair<>("b5e9cbb6fdce4b7e8c5cd6ad1addf6e4af35da67", "src/menu.h")
//                new Pair<>("f51b6486fc8b0e3fa7fd08cbf83b27ef0d5efe1a", "src/menu.h")
        ).map(p -> new FullyQualifiedPatch(p.first(), emacs, Path.of(p.second()))).toList();
    }

//    private static CommitHistoryAnalysisTask validate(final FullyQualifiedPatch fish) throws IOException {
//        final Git git = fish.commit().repo().getGitRepo().run();
//        Assert.assertNotNull(git);
//        final RevWalk revWalk = new RevWalk(git.getRepository());
//        final RevCommit commit = revWalk.parseCommit(ObjectId.fromString(fish.commit().hash()));
//        return Validation.VALIDATION_TASK_FACTORY.create(
//                fish.commit().repo(),
//                new GitDiffer(fish.commit().repo()),
//                OUTPUT_PATH,
//                List.of(commit)
//        );
//    }

    public static Pair<RevCommit, RevCommit> getChildParentCommit(final FullyQualifiedPatch p) throws IOException {
        final Git git = p.commit().repo().getGitRepo().run();
        Assert.assertNotNull(git);
        final RevWalk revWalk = new RevWalk(git.getRepository());
        final RevCommit childCommit = revWalk.parseCommit(ObjectId.fromString(p.commit().hash()));
        final RevCommit parentCommit = revWalk.parseCommit(childCommit.getParent(0).getId());
        return new Pair<>(childCommit, parentCommit);
    }

    private static void render(
            final PatchDiff patchDiff,
            final Path outputFile,
            final DiffTreeLineGraphExportOptions exportOptions) {
        final RenderOptions.Builder rob = new RenderOptions.Builder()
                .setCleanUpTemporaryFiles(false);

        if (exportOptions.nodeFormat() instanceof ReleaseMiningDiffNodeFormat) {
            rob.addExtraArguments(
                    "--format", "patternsrelease"
            );
        }

        if (!exportOptions.treeFilter().test(patchDiff.getDiffTree())) {
            Logger.info("The given DiffTree was filtered out.");
            return;
        }

        DiffTreeRenderer.WithinDiffDetective().render(
                patchDiff,
                outputFile,
                rob.build(),
                exportOptions);
    }

    public static Pair<String, PatchDiff> createPatch(final FullyQualifiedPatch patch) throws IOException {
        final String patchName = patch.file().toString().replaceAll("/", "_") + "@" + patch.commit().hash();
        final PatchDiff patchDiff = DiffTreeParser.parsePatch(patch.commit().repo(), patch.file().toString(), patch.commit().hash());

        assert patchDiff != null;
        IO.write(OUTPUT_PATH.resolve(patchName + ".diff"), patchDiff.getDiff());

        return new Pair<>(patchName, patchDiff);
    }

    private static void debugPatch(final String debugCaseName, final FullyQualifiedPatch patch, final DiffTreeLineGraphExportOptions exportOptions) throws IOException {
        Logger.info("Debugging " + debugCaseName);
        final Pair<String, PatchDiff> nameAndPatch = createPatch(patch);

//        nameAndPatch.second().getDiffTree().forAll(node -> {
//            System.out.println(node);
//        });

        render(nameAndPatch.second(), OUTPUT_PATH.resolve(debugCaseName), exportOptions);
    }

    private static void debugPlain(final FullyQualifiedPatch patch) throws IOException {
        debugPatch(
                "plain",
                patch,
                new DiffTreeLineGraphExportOptions(
                        GraphFormat.DIFFTREE
                        // We have to ensure that all DiffTrees have unique IDs, so use name of changed file and commit hash.
                        , new CommitDiffDiffTreeLabelFormat()
                        , new DebugDiffNodeFormat()
                        , new DefaultEdgeLabelFormat()
                        , new ExplainedFilter<>(DiffTreeFilter.notEmpty())
                        , List.of()
                        , DiffTreeLineGraphExportOptions.LogError()
                        .andThen(DiffTreeLineGraphExportOptions.RenderError())
                        .andThen(DiffTreeLineGraphExportOptions.SysExitOnError())
                ));
    }

    private static void debugFSE(final FullyQualifiedPatch patch) throws IOException {
        debugPatch("fse", patch, Validation.ValidationExportOptions(patch.commit().repo()));
    }

    private static void debugASE(final FullyQualifiedPatch patch) throws IOException {
        debugPatch("ase", patch, DiffTreeMiner.MiningExportOptions(patch.commit().repo()));
    }

    @Test
    public void achdugrueneneune() throws IOException {
        // https://github.com/emacs-mirror/emacs/commit/b5e9cbb6fdce4b7e8c5cd6ad1addf6e4af35da67#diff-f51b6486fc8b0e3fa7fd08cbf83b27ef0d5efe1a
        for (FullyQualifiedPatch fish : FISHY_COMMITS) {
            final Pair<RevCommit, RevCommit> childAndParentCommits = getChildParentCommit(fish);
            final String childHash = childAndParentCommits.first().getName();
            final String parentHash = childAndParentCommits.second().getId().getName();

            System.out.println("Debugging Patch: " + fish.file() + "@" + fish.commit().hash() + " in " + fish.commit().repo().getRepositoryName());
            String repoURL = fish.commit().repo().getRemoteURI().toString();
            repoURL = repoURL.substring(0, repoURL.length() - ".git".length());
            System.out.println("View on Github: " + repoURL + "/commit/" + childHash + "#diff-" + parentHash);

            debugPlain(fish);
            debugFSE(fish);
            debugASE(fish);
        }
    }
}

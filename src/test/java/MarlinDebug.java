import datasets.ParseOptions;
import datasets.Repository;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.parse.DiffTreeParser;
import diff.difftree.parse.IllFormedAnnotationException;
import diff.difftree.serialize.LineGraphExport;
import diff.difftree.transform.DiffTreeTransformer;
import feature.CPPAnnotationParser;
import mining.DiffTreeMiner;
import mining.dataset.MiningDataset;
import mining.dataset.MiningDatasetFactory;
import mining.strategies.CommitHistoryAnalysisTask;
import mining.strategies.MiningTask;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.prop4j.Node;
import org.tinylog.Logger;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.Clock;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MarlinDebug {
    private record RepoInspection(
            List<String> suspiciousCommits,
            Repository repo,
            Path outputPath
    ) {}

    private static final Path reposPath = Paths.get("..", "DiffDetectiveMining");
    private static final Path OUTPATH = Constants.RESOURCE_DIR.resolve("debug/test");

    private static RepoInspection MARLIN, PHP;

    @Before
    public void init() {
        {
            MiningDataset marlin = new MiningDataset(
                    "Marlin",
                    "https://github.com/MarlinFirmware/Marlin.git",
                    "3d printing",
                    ""
            );
            MARLIN = new RepoInspection(
                    List.of(
                            "226ee7c1f3e1b8f88759a1dc49f329ab9afb3270",
                            "9956e6267474c915c649ea3ad5d58791ac6e6fdc",
                            "c9561a88261afd14d9c013d2096e14e319c363a5",
                            "9ecfa1d2528a57eaa71a25acaac3e87fb45e0eb1",
                            "0e60c8b7e04a6cd2758108bcc80f2ab57deec23c"
                    ),
                    new MiningDatasetFactory(reposPath).create(marlin),
                    OUTPATH.resolve(marlin.name())
            );
            MARLIN.repo.setParseOptions(MARLIN.repo.getParseOptions().withDiffStoragePolicy(ParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF));
        }
        {
            MiningDataset php = new MiningDataset(
                    "php",
                    "https://github.com/php/php-src.git",
                    "program interpreter",
                    ""
            );
            PHP = new RepoInspection(
//                    List.of("e2182a1ba7cdd3c915cf29cd8367a6e02a0c10c8"),
                    List.of("16d7fd9d7f4849c88acbbfb04f7e09b7c58fd73f"),
                    new MiningDatasetFactory(reposPath).create(php),
                    OUTPATH.resolve(php.name())
            );
            PHP.repo.setParseOptions(PHP.repo.getParseOptions().withDiffStoragePolicy(ParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF));
        }
    }

    public static void testCommit(final RepoInspection repoInspection, final String commitHash) throws IOException {
        Logger.info(">>> Testing commit " + commitHash + " of " + repoInspection.repo.getRepositoryName());
        Logger.info("  Begin parsing");
        Clock clock = new Clock();
        final CommitDiff commitDiff = DiffTreeParser.parseCommit(repoInspection.repo, commitHash);
        Logger.info("  Done after " + clock.printPassedSeconds());
        final List<DiffTreeTransformer> transform = DiffTreeMiner.Postprocessing(repoInspection.repo);

        for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
            if (patch.isValid()) {
                Logger.info("  Begin processing " + patch);
                final DiffTree t = patch.getDiffTree();
                Logger.info("    Begin transform");
                clock.start();
                DiffTreeTransformer.apply(transform, t);
                Logger.info("    Done after " + clock.printPassedSeconds());
                Logger.info("    Begin elementary pattern matching");
                clock.start();
                t.forAll(node -> {
                    if (node.isCode()) {
                        try {
                            Logger.info(ProposedAtomicPatterns.Instance.match(node));
                        } catch (Exception e) {
                            //DiffTreeLineGraphExportOptions.RenderError().accept(patch, e);
                            Logger.error(e);
                            Logger.info("Died at node " + node.toString());
                            Logger.info("  before parent: " + node.getBeforeParent());
                            Logger.info("   after parent: " + node.getBeforeParent());
                            Logger.info("isAdd: " + node.isAdd());
                            Logger.info("isRem: " + node.isRem());
                            Logger.info("isNon: " + node.isNon());
                            Logger.info("isCode: " + node.isCode());
//                            throw e;
                            System.exit(0);
                        }
                    }
                });
                Logger.info("    Done after " + clock.printPassedSeconds());
                Logger.info("  End processing " + patch);
            }
        }

        StringBuilder bigB = new StringBuilder();
        LineGraphExport.toLineGraphFormat(commitDiff, bigB, DiffTreeMiner.MiningExportOptions(repoInspection.repo));
    }

    public static void asMiningTask(final RepoInspection repoInspection, final String commitHash) throws Exception {
        final Git git = repoInspection.repo.getGitRepo().run();
        Assert.assertNotNull(git);
        final RevWalk revWalk = new RevWalk(git.getRepository());
        final RevCommit childCommit = revWalk.parseCommit(ObjectId.fromString(commitHash));

        MiningTask m = new MiningTask(new CommitHistoryAnalysisTask.Options(
                repoInspection.repo,
                new GitDiffer(repoInspection.repo),
                repoInspection.outputPath,
                DiffTreeMiner.MiningExportOptions(repoInspection.repo),
                DiffTreeMiner.MiningStrategy(),
                List.of(childCommit)));
        m.call();
    }

    public static void asValidationTask(final RepoInspection repoInspection, final String commitHash) throws Exception {
        final Git git = repoInspection.repo.getGitRepo().run();
        Assert.assertNotNull(git);
        final RevWalk revWalk = new RevWalk(git.getRepository());
        final RevCommit childCommit = revWalk.parseCommit(ObjectId.fromString(commitHash));

        DiffTreeMiner.Validate().create(
                repoInspection.repo,
                new GitDiffer(repoInspection.repo),
                repoInspection.outputPath,
                List.of(childCommit)
        ).call();
    }

    public static void test(final RepoInspection repoInspection) throws Exception {
        for (final String spooky : repoInspection.suspiciousCommits) {
            testCommit(repoInspection, spooky);
//            asValidationTask(repoInspection, spooky);
//            asMiningTask(repoInspection, spooky);
        }
    }

    @Test
    public void testMarlin() throws Exception {
        test(MARLIN);
    }

    @Test
    public void testPHP() throws Exception {
        test(PHP);
    }

    @Test
    public void testFormulaParsing() throws IllFormedAnnotationException {
        final String original = "#if defined ( a ) && x > 200 + 2 && A && (foo(3, 4) || bar ( 3 , 4, 9, baz(3)) || z==3-1) && h<=7/2 && x == 4 % 2 && m>=k && k<11*3";
//        String adapted = original;
//        adapted = adapted.replaceAll("\\(\\)", "");
//        adapted = adapted.replaceAll("(\\w+)\\s*\\(\\s*((\\w+\\s*,\\s*)*)(\\w*\\s*)\\)", );
        System.out.println("Original Formula: " + original);

        final Node n = CPPAnnotationParser.Default.parseDiffLine(original);
        System.out.println("  Parsed Formula: " + n);
        System.out.println("        Literals: " + n.getLiterals());
    }
}

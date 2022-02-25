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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MarlinDebug {
    private static final List<String> SUSPICIOUS_COMMITS = List.of(
            "226ee7c1f3e1b8f88759a1dc49f329ab9afb3270",
            "9956e6267474c915c649ea3ad5d58791ac6e6fdc",
            "c9561a88261afd14d9c013d2096e14e319c363a5",
            "9ecfa1d2528a57eaa71a25acaac3e87fb45e0eb1",
            "0e60c8b7e04a6cd2758108bcc80f2ab57deec23c"
    );

    private static Repository repository;
    private static final Path repoPath = Paths.get("..", "DiffDetectiveMining");
    private static final Path outputPath = Constants.RESOURCE_DIR.resolve("debug/test");

    @Before
    public void init() {
        MiningDataset marlin = new MiningDataset(
                "Marlin",
                "https://github.com/MarlinFirmware/Marlin.git",
                "3d printing",
                "keine Ahnung man"
        );
        repository = new MiningDatasetFactory(repoPath).create(marlin);
        repository.setParseOptions(repository.getParseOptions().withDiffStoragePolicy(ParseOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF));
    }

    public static void testCommit(final String commitHash) throws IOException {
        final CommitDiff commitDiff = DiffTreeParser.parseCommit(repository, commitHash);
        final List<DiffTreeTransformer> transform = DiffTreeMiner.Postprocessing(repository);

        for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
            if (patch.isValid()) {
                Logger.info("Begin processing " + patch);
                final DiffTree t = patch.getDiffTree();
                DiffTreeTransformer.apply(transform, t);
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
                Logger.info("End processing " + patch);
            }
        }

        StringBuilder bigB = new StringBuilder();
        LineGraphExport.toLineGraphFormat(commitDiff, bigB, DiffTreeMiner.MiningExportOptions(repository));
    }

    public static void asMiningTask(final String commitHash) throws Exception {
        final Git git = repository.getGitRepo().run();
        Assert.assertNotNull(git);
        final RevWalk revWalk = new RevWalk(git.getRepository());
        final RevCommit childCommit = revWalk.parseCommit(ObjectId.fromString(commitHash));

        MiningTask m = new MiningTask(new CommitHistoryAnalysisTask.Options(
                repository,
                new GitDiffer(repository),
                outputPath,
                DiffTreeMiner.MiningExportOptions(repository),
                DiffTreeMiner.MiningStrategy(),
                List.of(childCommit)));
        m.call();
    }

    @Test
    public void test() throws Exception {
        for (final String spooky : SUSPICIOUS_COMMITS) {
//            testCommit(spooky);
            asMiningTask(spooky);
        }
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

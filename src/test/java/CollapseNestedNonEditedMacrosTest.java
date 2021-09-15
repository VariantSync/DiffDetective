import diff.CommitDiff;
import diff.GitDiffer;
import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.transform.CollapseNestedNonEditedMacros;
import diff.difftree.transform.CollapseNonEditedSubtrees;
import diff.difftree.transform.DiffTreeTransformer;
import diff.difftree.transform.NaiveMovedCodeDetection;
import diff.serialize.LineGraphExport;
import load.GitLoader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CollapseNestedNonEditedMacrosTest {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/collapse");
    private static final Path genDir = resDir.resolve("gen");
    private static final List<DiffTreeTransformer> transformers = List.of(
            new CollapseNonEditedSubtrees(),
            new CollapseNestedNonEditedMacros(),
            new CollapseNonEditedSubtrees()
    );
    private static final DiffTreeRenderer.RenderOptions renderOptions = new DiffTreeRenderer.RenderOptions(
            LineGraphExport.NodePrintStyle.Mappings,
            false
            );

    private void transformAndRender(String diffFileName) throws IOException {
        final DiffTree t = DiffTree.fromFile(resDir.resolve(diffFileName), true, true);
        transformAndRender(t, diffFileName);
    }

    private void transformAndRender(DiffTree diffTree, String name) throws IOException {
        final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();

        renderer.render(diffTree, name + "_0", genDir, renderOptions);
        int i = 1;
        for (DiffTreeTransformer f : transformers) {
            f.transform(diffTree);
            renderer.render(diffTree, name + "_" + i, genDir, renderOptions);
            ++i;
        }
    }

    @Before
    public void init() {
        DiffTreeTransformer.checkDependencies(transformers);
    }

    @Test
    public void simpleTest() throws IOException {
        transformAndRender("simple.txt");
    }

    @Test
    public void elifTest() throws IOException {
        transformAndRender("elif.txt");
    }

    private void testCommit(String file, String commitHash) throws IOException {
        final String repo = "Marlin_old.zip";
        final boolean saveMemory = true;

        final Git git = GitLoader.fromZip(repo);
        assert git != null;
        final RevWalk revWalk = new RevWalk(git.getRepository());
        final RevCommit childCommit = revWalk.parseCommit(ObjectId.fromString(commitHash));
        final RevCommit parentCommit = revWalk.parseCommit(childCommit.getParent(0).getId());

        final CommitDiff commitDiff = GitDiffer.createCommitDiff(
                git,
                Main.DefaultDiffFilterForMarlin,
                parentCommit,
                childCommit,
                !saveMemory);

        for (final PatchDiff pd : commitDiff.getPatchDiffs()) {
            if (file.equals(pd.getFileName())) {
                transformAndRender(pd.getDiffTree(), file);
                return;
            }
        }

        Assert.fail("Did not find file \"" + file + "\" in commit " + commitHash + "!");
    }

    @Test
    public void testWurmcoil() throws IOException {
        testCommit("Marlin/pins.h", "d6d6fb8930be8d0b3bd34592c915732937c6f4d9");
    }

    @Test
    public void testConfiguration_adv() throws IOException {
        testCommit("Marlin/example_configurations/RigidBot/Configuration_adv.h", "d3fe3a0962fdbdcd9548abaf765e0cff72d9cf8d");
    }
}

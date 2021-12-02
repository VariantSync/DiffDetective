package diff.difftree.transform;

import diff.PatchDiff;
import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.render.PatchDiffRenderer;
import diff.serialize.LineGraphExport;
import org.pmw.tinylog.Logger;
import util.Assert;

import java.nio.file.Path;
import java.util.function.Predicate;

public class ExampleFinder implements DiffTreeTransformer {
    public static final DiffTreeRenderer.RenderOptions ExportOptions = new DiffTreeRenderer.RenderOptions(
            LineGraphExport.NodePrintStyle.Mappings,
            false,
            1000,
            DiffTreeRenderer.RenderOptions.DEFAULT.nodesize()/3,
            0.5*DiffTreeRenderer.RenderOptions.DEFAULT.edgesize(),
            DiffTreeRenderer.RenderOptions.DEFAULT.arrowsize()/2,
            2,
            true
    );

    private final Predicate<DiffTree> isGoodExample;
    private final PatchDiffRenderer exampleExport;

    public ExampleFinder(final Predicate<DiffTree> isGoodExample, final Path outDir, DiffTreeRenderer renderer) {
        this.isGoodExample = isGoodExample;
        this.exampleExport = new PatchDiffRenderer(outDir, renderer, ExportOptions);
    }

    @Override
    public void transform(DiffTree diffTree) {
        if (isGoodExample.test(diffTree)) {
            Assert.assertTrue(diffTree.getSource() instanceof PatchDiff);
            Logger.info("Exporting example candidate: " + diffTree.getSource());
            exampleExport.render((PatchDiff) diffTree.getSource());
        }
    }
}

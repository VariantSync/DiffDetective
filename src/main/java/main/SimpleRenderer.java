package main;

import diff.difftree.DiffTree;
import diff.difftree.parse.DiffNodeParser;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import diff.difftree.serialize.nodeformat.DebugDiffNodeFormat;
import diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.tinylog.Logger;
import util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SimpleRenderer {
    private static final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
    private static final DiffTreeRenderer.RenderOptions renderOptions = new DiffTreeRenderer.RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
//            new ReleaseMiningDiffNodeFormat(),
            new MappingsDiffNodeFormat(),
            new DefaultEdgeLabelFormat(),
            true,
            DiffTreeRenderer.RenderOptions.DEFAULT.dpi() / 2,
            3*DiffTreeRenderer.RenderOptions.DEFAULT.nodesize(),
            2*DiffTreeRenderer.RenderOptions.DEFAULT.edgesize(),
            2*DiffTreeRenderer.RenderOptions.DEFAULT.arrowsize(),
            8,
            true,
//            List.of("--format", "patternsrelease")
            List.of()
    );
    private static final DiffTreeRenderer.RenderOptions renderExampleOptions = new DiffTreeRenderer.RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new DebugDiffNodeFormat(),
            new DefaultEdgeLabelFormat(),
            true,
            DiffTreeRenderer.RenderOptions.DEFAULT.dpi(),
            3*DiffTreeRenderer.RenderOptions.DEFAULT.nodesize(),
            2*DiffTreeRenderer.RenderOptions.DEFAULT.edgesize(),
            2*DiffTreeRenderer.RenderOptions.DEFAULT.arrowsize(),
            8,
            true,
            List.of()
    );
    private final static boolean collapseMultipleCodeLines = true;
    private final static boolean ignoreEmptyLines = true;

    private static void render(final Path fileToRender) {
        if (fileToRender.toString().endsWith(".lg")) {
            Logger.info("Rendering " + fileToRender);
            renderer.renderFile(fileToRender, renderExampleOptions);
        } else if (fileToRender.toString().endsWith(".diff")) {
            Logger.info("Rendering " + fileToRender);
            final DiffTree t;
            try {
                t = DiffTree.fromFile(fileToRender, collapseMultipleCodeLines, ignoreEmptyLines, DiffNodeParser.Default).unwrap().getSuccess();
            } catch (IOException e) {
                System.err.println("Could not read given file \"" + fileToRender + "\" because:\n" + e.getMessage());
                return;
            }
            renderer.render(t, fileToRender.getFileName().toString(), fileToRender.getParent(), renderExampleOptions);
        } else {
            Logger.warn("Skipping unsupported file " + fileToRender);
        }

    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Expected path to diff or lg file as argument.");
            return;
        }

        final Path fileToRender = Path.of(args[0]);

        if (!Files.exists(fileToRender)) {
            Logger.error("Path " + fileToRender + " does not exist!");
            return;
        }

        Logger.info("Rendering " + (Files.isDirectory(fileToRender) ? "directory " : "file ") + fileToRender);

        if (Files.isDirectory(fileToRender)) {
            FileUtils.listAllFilesRecursively(fileToRender).forEach(SimpleRenderer::render);
        } else {
            render(fileToRender);
        }

        System.out.println("done");
    }
}

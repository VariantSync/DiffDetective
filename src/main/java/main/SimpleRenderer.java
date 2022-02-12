package main;

import diff.difftree.DiffTree;
import diff.difftree.parse.DiffNodeParser;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.render.RenderOptions;
import diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import org.tinylog.Logger;
import util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SimpleRenderer {
    private static final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
    private static final RenderOptions renderOptions = new RenderOptions.Builder()
//            .setNodeFormat(new ReleaseMiningDiffNodeFormat()),
            .setNodeFormat(new MappingsDiffNodeFormat())
            .setDpi(RenderOptions.DEFAULT.dpi() / 2)
            .setNodesize(3*RenderOptions.DEFAULT.nodesize())
            .setEdgesize(2*RenderOptions.DEFAULT.edgesize())
            .setArrowsize(2*RenderOptions.DEFAULT.arrowsize())
            .setFontsize(8)
//            .addExtraArguments("--format", "patternsrelease")
            .build();

    private static final RenderOptions renderExampleOptions = new RenderOptions.Builder()
            .setNodesize(3*RenderOptions.DEFAULT.nodesize())
            .setEdgesize(2*RenderOptions.DEFAULT.edgesize())
            .setArrowsize(2*RenderOptions.DEFAULT.arrowsize())
            .setFontsize(8)
            .addExtraArguments("--startlineno", "4201")
            .build();

    private final static boolean collapseMultipleCodeLines = true;
    private final static boolean ignoreEmptyLines = true;
    private final static List<String> SUPPORTED_FILE_TYPES = List.of(".diff", ".c", ".cpp", ".h", ".hpp");

    private static void render(final Path fileToRender) {
        final String fileToRenderStr = fileToRender.toString();
        if (fileToRenderStr.endsWith(".lg")) {
            Logger.info("Rendering " + fileToRender);
            renderer.renderFile(fileToRender, renderExampleOptions);
        } else if (SUPPORTED_FILE_TYPES.stream().anyMatch(fileToRenderStr::endsWith)) {
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

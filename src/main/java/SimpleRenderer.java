import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;

import java.io.IOException;
import java.nio.file.Path;

public class SimpleRenderer {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Expected path to diff or lg file as argument.");
            return;
        }

        final Path fileToRender = Path.of(args[0]);
        final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();

        if (fileToRender.endsWith(".lg")) {
            renderer.renderFile(fileToRender);
        } else {
            final DiffTree t;
            try {
                t = DiffTree.fromFile(fileToRender, false, true);
            } catch (IOException e) {
                System.err.println("Could not read given file \"" + fileToRender + "\" because:\n" + e.getMessage());
                return;
            }
            renderer.render(t, fileToRender.getFileName().toString(), fileToRender.getParent());
        }

        System.out.println("done");
    }
}

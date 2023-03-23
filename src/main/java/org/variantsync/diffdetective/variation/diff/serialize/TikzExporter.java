package org.variantsync.diffdetective.variation.diff.serialize;

import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.util.LaTeX;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Exporter for TikZ pictures which can be embedded into a LaTeX document.
 *
 * The resulting graph is styled using TikZ styles which have to be set using {@code \tikzset}. An
 * example for all required styles can be found in the file {@code tikz_header.tex} in the resource
 * directory. This particular style is used by {@link exportFullLatexExample}.
 */
public final class TikzExporter implements Exporter {
    private Format format;

    public TikzExporter(Format format) {
        this.format = format;
    }

    /**
     * Export {@code diffTree} as TikZ graph into {@code destination}.
     *
     * The exported graph starts and ends with the {@code tikzpicture} environment and does not
     * include surrounding LaTeX code like the preamble. For a quick way to set up a working LaTeX
     * document see {@link exportFullLatexExample}.
     *
     * @param diffTree to be exported
     * @param destination where the result should be written
     */
    @Override
    public void exportDiffTree(DiffTree diffTree, OutputStream destination) throws IOException {
        exportDiffTree(diffTree, GraphvizExporter.LayoutAlgorithm.DOT, destination);
    }

    /**
     * Export {@code diffTree} as TikZ graph into {@code destination}.
     *
     * Same as {@link exportDiffTree(DiffTree, OutputStream)}, but allows the selection of
     * a different Graphviz layout algorithm.
     */
    public void exportDiffTree(
            DiffTree diffTree,
            GraphvizExporter.LayoutAlgorithm algorithm,
            OutputStream destination
            ) throws IOException {
        final Map<Integer, DiffNode> ids = new HashMap<>();
        diffTree.forAll(node -> ids.put(node.getID(), node));

        // Convert the layout information received by Graphviz to coordinates used by TikZ.
        final Map<DiffNode, Vec2> positions = new HashMap<>();
        GraphvizExporter.layoutNodesIn(diffTree, format, algorithm, (id, x, y) ->
                positions.put(ids.get(id), new Vec2(x, y))
        );

        exportDiffTree(
                diffTree,
                positions::get,
                destination,
                true);
    }

    public void exportDiffTree(
            DiffTree diffTree,
            Function<DiffNode, Vec2> nodeLayout,
            OutputStream destination,
            boolean escape
    ) {
        // Start tikz picture.
        var output = new PrintStream(destination);
        output.println("\\begin{tikzpicture}");

        // Convert the layout information received by Graphviz to coordinates used by TikZ.
        diffTree.forAll(node -> {
            final Vec2 position = nodeLayout.apply(node);
            output.format("\t\\coordinate (%s) at (%s,%s);%n", node.getID(), position.x(), position.y());
        });

        // Add all TikZ nodes positioned at the Graphviz coordinates.
        format.forEachNode(diffTree, (node) -> {
            output.format("%n\t\\node[%s, %s] (node_%s) at (%s) {};",
                node.isArtifact() ? "artifact" : "annotation",
                node.getDiffType().toString().toLowerCase(Locale.ROOT),
                node.getID(),
                node.getID());
        });
        output.println();

        // Add all TikZ edges positioned.
        output.format("%n\t\\draw[vtdarrow]");
        format.forEachEdge(diffTree, (edge) -> {
            output.format("%n\t\t(node_%d) edge[%s] (node_%d)",
                    edge.from().getID(),
                    edge.style().tikzStyle(),
                    edge.to().getID());
        });
        output.println();
        output.format("%n\t;");
        output.println();

        // Draw node labels. We do this last so that they are on top of edges and nodes.
        format.forEachNode(diffTree, (node) -> {
            Stream<String> labels =
                    format
                            .getNodeFormat()
                            .toMultilineLabel(node)
                            .stream();
            if (escape) {
                labels = labels.map(LaTeX::escape);
            }
            String escapedLabel = labels.collect(Collectors.joining(" \\\\ "));

            output.format("\t\\node[textbox] at (%s) {%s};%n",
                    node.getID(),
                    escapedLabel);
        });

        // Finish the TikZ picture.
        output.println("\\end{tikzpicture}");
        output.flush();
    }

    /**
     * Exports a ready to compile LaTeX document containing a TikZ graph exported by {@link
     * exportDiffTree}.
     *
     * The resulting document should be used as prototype not as ready to be published
     * visualisation. To discourage further processing the API differs from {@code exportDiffTree}
     * by exporting directly into a file.
     *
     * @param diffTree to be exported
     * @param destination path of the generated file
     */
    public void exportFullLatexExample(DiffTree diffTree, Path destination) throws IOException {
        try (var file = Files.newOutputStream(destination)) {
            try (var header = new BufferedInputStream(getClass().getResourceAsStream("/tikz_header.tex"))) {
                header.transferTo(file);
            }

            exportDiffTree(diffTree, file);

            try (var footer = new BufferedInputStream(getClass().getResourceAsStream("/tikz_footer.tex"))) {
                footer.transferTo(file);
            }
        }
    }
}

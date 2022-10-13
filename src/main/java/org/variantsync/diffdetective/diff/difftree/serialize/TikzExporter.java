package org.variantsync.diffdetective.diff.difftree.serialize;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.util.LaTeX;

/**
 * Exporter for TikZ pictures which can be embedded into a LaTeX document.
 *
 * The resulting graph is styled using TikZ styles which have to be set using {@code \tikzset}. An
 * example for all required styles can be found in the file {@code tikz_header.tex} in the resource
 * directory. This particular style is used by {@link exportFullLatexExample}.
 */
public final class TikzExporter implements Exporter {
    private static final Pattern graphvizNodePattern = Pattern.compile("^node (\\w+) ([0-9.]+) ([0-9.]+) .*$");
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
        // Start tikz picture.
        var output = new PrintStream(destination);
        output.println("\\begin{tikzpicture}");

        // Convert the layout information received by Graphviz to coordinates used by TikZ.
        try (
                var graphvizExporter = new GraphvizExporter(format)
                    .computeGraphvizLayout(
                        diffTree,
                        algorithm,
                        GraphvizExporter.OutputFormat.PLAIN);
                var unbufferedGraphvizOutput = new InputStreamReader(graphvizExporter);
                var graphizOutput = new BufferedReader(unbufferedGraphvizOutput
        )) {
            // Skip scale and dimensions
            graphizOutput.readLine();

            String line;
            while ((line = graphizOutput.readLine()) != null) {
                var nodeMatcher = graphvizNodePattern.matcher(line);
                if (nodeMatcher.matches()) {
                    var id = nodeMatcher.group(1);
                    var x = nodeMatcher.group(2);
                    var y = nodeMatcher.group(3);

                    output.format("\t\\coordinate (%s) at (%s,%s);%n", id, x, y);
                }
            }
        }

        // Add all TikZ nodes positioned at the Graphviz coordinates.
        format.forEachNode(diffTree, (node) -> {
            String escapedLabel =
                format
                    .getNodeFormat()
                    .toMultilineLabel(node)
                    .stream()
                    .map(LaTeX::escape)
                    .collect(Collectors
                    .joining(" \\\\ "));

            output.format("%n\t\\node[%s, %s] (node_%s) at (%s) {};%n",
                node.isArtifact() ? "artifact" : "annotation",
                node.getDiffType().toString().toLowerCase(Locale.ROOT),
                node.getID(),
                node.getID());
            output.format("\t\\node[textbox] at (%s) {%s};%n",
                node.getID(),
                escapedLabel);
        });

        // Add all TikZ edges positioned.
        output.format("%n\t\\draw[vtdarrow]");
        format.forEachEdge(diffTree, (edge) -> {
            output.format("%n\t\t(node_%d) edge[%s] (node_%d)",
                edge.from().getID(),
                edge.style().tikzStyle(),
                edge.to().getID());
        });
        output.println(";");

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

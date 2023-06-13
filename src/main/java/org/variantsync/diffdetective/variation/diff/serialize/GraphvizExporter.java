package org.variantsync.diffdetective.variation.diff.serialize;

import org.variantsync.diffdetective.util.TriConsumer;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffTree;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Exporter for the Graphviz dot format.
 * Using this exporter the Graphviz application can be used to layout a {@link DiffTree} for
 * visualisation.
 *
 * Currently only basic layout relevant information is exported, so if the result is rendered directly by Graphviz no styling is applied.
 */
public class GraphvizExporter<L extends Label> implements Exporter<L> {
    private static final Pattern quotePattern = Pattern.compile("[\"\\\\]");
    public static final Pattern graphvizNodePattern = Pattern.compile("^node (\\w+) ([0-9.]+) ([0-9.]+) .*$");
    private Format<? super L> format;

    public enum LayoutAlgorithm {
        DOT("dot"),
        NEATO("neato"),
        TWOPI("twopi"),
        CIRCO("circo"),
        FDP("fdp"),
        SFDP("sfdp"),
        PATCHWORK("patchwork"),
        OSAGE("osage");

        private final String executableName;

        private LayoutAlgorithm(String executableName) {
            this.executableName = executableName;
        }

        public String getExecutableName() {
            return executableName;
        }
    }

    public enum OutputFormat {
        // Graphviz supports way more output formats. Add them when necessary.
        JPG("jpg"),
        JSON("json"),
        PDF("pdf"),
        PLAIN("plain"),
        PLAIN_EXT("plain-ext"),
        PNG("png"),
        SVG("svg");

        private final String formatName;

        private OutputFormat(String formatName) {
            this.formatName = formatName;
        }

        public String getFormatName() {
            return formatName;
        }
    }

    public GraphvizExporter(Format<? super L> format) {
        this.format = format;
    }

    /**
     * Export {@code diffTree} as Graphviz graph into {@code destination}.
     * The exported graph is unstyled, but includes all necessary layout information.
     *
     * @param diffTree to be exported
     * @param destination where the result should be written
     */
    @Override
    public <La extends L> void exportDiffTree(DiffTree<La> diffTree, OutputStream destination) throws IOException {
        var output = new PrintStream(destination);

        output.println("digraph g {");

        format.forEachNode(diffTree, (node) -> {
            output.format("  %d [label=\"%s\"];%n",
                    node.getID(),
                    escape(format.getNodeFormat().toMultilineLabel(node)));
        });

        format.forEachEdge(diffTree, (edge) -> {
            output.format("  %d -> %d;%n", edge.from().getID(), edge.to().getID());
        });

        output.println("}");
        output.flush();
    }

    /**
     * Runs the Graphviz {@code dot} program returning its result.
     *
     * @param diffTree is the tree to be layouted by Graphviz.
     * @param algorithm the layout algorithm used by Graphviz
     * @param outputFormat is the requested format which is passed to the {@code dot} program with
     * the {@code -T} flag.
     * @return a buffered {@code InputStream} of the Graphviz output
     */
    public <La extends L> InputStream computeGraphvizLayout(
            DiffTree<La> diffTree,
            LayoutAlgorithm algorithm,
            OutputFormat outputFormat)
            throws IOException {
        // Print error messages to stderr so grogramming errors in {@code exportDiffTree} can be
        // diagnosed more easily.
        var graphvizProcess =
            new ProcessBuilder(algorithm.getExecutableName(), "-T" + outputFormat.getFormatName())
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        // This could lead to a dead lock if {@code graphvizProcess} is not consuming its input and
        // the OS buffer fills up, but Graphviz needs the whole graph to generate a layout so this
        // should be safe.
        var graphizInput = new BufferedOutputStream(graphvizProcess.getOutputStream());
        exportDiffTree(diffTree, graphizInput);
        graphizInput.close();

        return new BufferedInputStream(graphvizProcess.getInputStream());
    }

    /**
     * Replaces all special characters with uninterpreted escape codes.
     *
     * The Graphviz parser for strings interprets some characters specially. The result of this
     * function can be used in Graphviz strings (surrounded by double quotes) resulting in all
     * characters appearing literally in the output.
     *
     * Note that some backends of Graphviz may still interpret some strings specially, most
     * commonly strings containing HTML tags.
     *
     * @param label a list of lines to be used as verbatim label
     * @return a single string which produces the lines of {@code label} verbatim
     */
    static private String escape(List<String> label) {
        return label
            .stream()
            .map((line) -> quotePattern.matcher(line).replaceAll((match) ->
                Matcher.quoteReplacement("\\" + match.group())
            ))
            .collect(Collectors.joining("\\n"));
    }

    public static <L extends Label> void layoutNodesIn(
            final DiffTree<L> diffTree,
            Format<? super L> format,
            GraphvizExporter.LayoutAlgorithm algorithm,
            TriConsumer<Integer, Double, Double> positionNode) throws IOException {
        // Convert the layout information received by Graphviz to coordinates used by TikZ.
        try (
                var graphvizExporter = new GraphvizExporter<>(format)
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
                    var id = Integer.parseInt(nodeMatcher.group(1));
                    var x  = Double.parseDouble(nodeMatcher.group(2));
                    var y  = Double.parseDouble(nodeMatcher.group(3));
                    positionNode.accept(id, x, y);
                }
            }
        }
    }
}

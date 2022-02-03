package mining.postprocessing;

import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.render.RenderOptions;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.LineGraphImport;
import diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import diff.difftree.serialize.treeformat.IndexedTreeFormat;
import mining.DiffTreeMiner;
import util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Performs a postprocessing on mined frequent subgraphs in edits to find semantic edit patterns.
 */
public class MiningPostprocessing {
    private static final DiffTreeRenderer DefaultRenderer = DiffTreeRenderer.WithinDiffDetective();
    private static final DiffTreeLineGraphImportOptions IMPORT_OPTIONS = new DiffTreeLineGraphImportOptions(
            GraphFormat.DIFFGRAPH,
            new IndexedTreeFormat(),
            DiffTreeMiner.NodeFormat(),
            DiffTreeMiner.EdgeFormat()
            );
    private static final RenderOptions DefaultRenderOptions = new RenderOptions(
            GraphFormat.DIFFTREE,
            IMPORT_OPTIONS.treeFormat(),
            IMPORT_OPTIONS.nodeFormat(),
            new DefaultEdgeLabelFormat(),
            false,
            RenderOptions.DEFAULT.dpi(),
            RenderOptions.DEFAULT.nodesize(),
            RenderOptions.DEFAULT.edgesize(),
            RenderOptions.DEFAULT.arrowsize(),
            RenderOptions.DEFAULT.fontsize(),
            true,
            List.of()
    );

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected path to directory of mined patterns as first argument and path to output directory as second argument but got only " + args.length + " arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        final Path outputPath = Path.of(args[1]);
        if (!Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Expected path to directory of mined patterns as first argument but got a path that is not a directory, namely \"" + inputPath + "\"!");
        }
        if (!FileUtils.tryIsEmptyDirectory(outputPath)) {
            throw new IllegalArgumentException("Expected path to an empty output directory as second argument but got a path that is not a directory or not empty, namely \"" + outputPath + "\"!");
        }

        postprocessAndInterpretResults(
                parseFrequentSubgraphsIn(inputPath),
                Postprocessor.Default(),
                System.out::println,
                DefaultRenderer,
                DefaultRenderOptions,
                outputPath
        );
    }

    /**
     * Parses all linegraph files in the given directory as patterns (i.e., as DiffGraphs).
     * non-recursive
     * @param directory A directory containing linegraph files.
     * @return The list of all diffgraphs parsed from linegraph files in the given directory.
     * @throws IOException If the directory could not be accessed ({@link Files::list}).
     */
    public static List<DiffTree> parseFrequentSubgraphsIn(final Path directory) throws IOException {
        return Files.list(directory)
                .filter(FileUtils::isLineGraph)
                .flatMap(path -> LineGraphImport.fromFile(path, IMPORT_OPTIONS).stream())
                .collect(Collectors.toList());
    }

    public static void postprocessAndInterpretResults(
            final List<DiffTree> frequentSubgraphs,
            final Postprocessor postprocessor,
            final Consumer<String> printer,
            final DiffTreeRenderer renderer,
            RenderOptions renderOptions,
            final Path outputDir)
    {
        final Postprocessor.Result result = postprocessor.postprocess(frequentSubgraphs);
        final List<DiffTree> semanticPatterns = result.processedTrees();

        printer.accept("Of " + frequentSubgraphs.size() + " mined subgraphs "
                + semanticPatterns.size() + " are candidates for semantic patterns.");
        printer.accept("Subgraphs were discarded for the following reasons:");
        for (Map.Entry<String, Integer> nameAndCount : result.filterCounts().entrySet()) {
            printer.accept("    " + nameAndCount.getKey() + ": " + nameAndCount.getValue());
        }
        printer.accept("");

        if (renderer != null) {
            if (renderOptions == null) {
                renderOptions = RenderOptions.DEFAULT;
            }

            printer.accept("Exporting and rendering semantic patterns to " + outputDir);
            int patternNo = 0;
            for (final DiffTree semanticPattern : semanticPatterns) {
                renderer.render(semanticPattern, "SemanticPattern " + patternNo, outputDir, renderOptions);
                ++patternNo;
            }
        }
    }
}

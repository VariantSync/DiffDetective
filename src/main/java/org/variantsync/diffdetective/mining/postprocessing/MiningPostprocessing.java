package org.variantsync.diffdetective.mining.postprocessing;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.render.RenderOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.*;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.IndexedTreeFormat;
import org.variantsync.diffdetective.mining.DiffTreeMiner;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Performs a postprocessing on mined frequent subgraphs in edits to find edit classes.
 */
public class MiningPostprocessing {
    private static final DiffTreeRenderer DefaultRenderer = DiffTreeRenderer.WithinDiffDetective();
    private static final boolean RENDER_CANDIDATES = false;
    private static final LineGraphImportOptions IMPORT_OPTIONS = new LineGraphImportOptions(
            GraphFormat.DIFFGRAPH,
            new IndexedTreeFormat(),
            DiffTreeMiner.NodeFormat(),
            DiffTreeMiner.EdgeFormat()
    );
    private static final LineGraphExportOptions EXPORT_OPTIONS = new LineGraphExportOptions(
            GraphFormat.DIFFTREE,
            IMPORT_OPTIONS.treeFormat(),
            DiffTreeMiner.NodeFormat(),
            DiffTreeMiner.EdgeFormat()
    );
    public static final RenderOptions DefaultRenderOptions = new RenderOptions.Builder()
            .setGraphFormat(EXPORT_OPTIONS.graphFormat())
            .setTreeFormat(EXPORT_OPTIONS.treeFormat())
            .setNodeFormat(EXPORT_OPTIONS.nodeFormat())
            .setEdgeFormat(EXPORT_OPTIONS.edgeFormat())
            .setCleanUpTemporaryFiles(false)
            .addExtraArguments("--format", "patternsrelease")
            .build();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected path to directory of mined patterns as first argument and path to output directory as second argument but got only " + args.length + " arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        final Path outputPath = Path.of(args[1]);
        if (!Files.isDirectory(inputPath) && !FileUtils.isLineGraph(inputPath)) {
            throw new IllegalArgumentException("Expected path to directory of mined patterns as first argument but got a path that is not a directory, namely \"" + inputPath + "\"!");
        }
        if (Files.exists(outputPath) && !FileUtils.tryIsEmptyDirectory(outputPath)) {
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

//        for (Map.Entry<NodeType, Integer> entry : LineGraphImport.countRootTypes.entrySet()) {
//            System.out.println(entry);
//        }
    }

    /**
     * Parses all linegraph files in the given directory or file as patterns (i.e., as DiffGraphs).
     * non-recursive
     * @param path A path to a linegraph file or a directory containing linegraph files.
     * @return The list of all diffgraphs parsed from linegraph files in the given directory.
     * @throws IOException If the directory could not be accessed ({@link Files#list}).
     */
    public static List<DiffTree> parseFrequentSubgraphsIn(final Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try {
                return Files.list(path)
                        .filter(FileUtils::isLineGraph)
                        .flatMap(file -> {
                            try {
                                return LineGraphImport.fromFile(file, IMPORT_OPTIONS).stream();
                            } catch (IOException e) {
                                // Checked exceptions can't be propagated because {@code flatMap}
                                // needs a {@code Function} which does not throw any checked
                                // exceptions.
                                throw new UncheckedIOException(e);
                            }
                        })
                        .collect(Collectors.toList());
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        } else {
            return LineGraphImport.fromFile(path, IMPORT_OPTIONS);
        }
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

        if (RENDER_CANDIDATES && renderer != null) {
            if (renderOptions == null) {
                renderOptions = RenderOptions.DEFAULT;
            }

            printer.accept("Exporting and rendering semantic patterns to " + outputDir);
            int patternNo = 0;
            for (final DiffTree semanticPattern : semanticPatterns) {
                renderer.render(semanticPattern, "SemanticPatternCandidate_" + patternNo, outputDir, renderOptions);
                ++patternNo;
            }
        } else {
            Path destinationPath = outputDir.resolve("candidates.lg");

            try (var destination = IO.newBufferedOutputStream(destinationPath)) {
                LineGraphExport.toLineGraphFormat(semanticPatterns, EXPORT_OPTIONS, destination);
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }
}

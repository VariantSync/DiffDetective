package org.variantsync.diffdetective.internal;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.GraphvizExporter;
import org.variantsync.diffdetective.variation.diff.serialize.TikzExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TextDiffToTikz(boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) {
    public static String[] UNICODE_PROP_SYMBOLS = new String[]{"¬", "∧", "∨", "⇒", "⇔"};

    /**
     * Format used for the test export.
     */
    private final static Format<DiffLinesLabel> format =
            new Format<DiffLinesLabel>(
                    TextDiffToTikz::tikzNodeLabel,
                    // There is a bug in the exporter currently that accidentally switches direction so as a workaround we revert it here.
                    new DefaultEdgeLabelFormat<>(EdgeLabelFormat.Direction.ParentToChild)
            );

    public static void main(String[] args) throws IOException, DiffParseException {
        if (args.length < 1) {
            System.err.println("Expected a path to a diff file or directory as first argument.");
            return;
        }

        final Path fileToConvert = Path.of(args[0]);
        if (!Files.exists(fileToConvert)) {
            Logger.error("Path {} does not exist!", fileToConvert);
            return;
        }

        final GraphvizExporter.LayoutAlgorithm layout;
        if (args.length < 2) {
            layout = GraphvizExporter.LayoutAlgorithm.DOT;
        } else {
            layout = GraphvizExporter.LayoutAlgorithm.valueOf(args[1].toUpperCase());
        }

        // FIXME: Use a dedicated argument parser in the future.
        boolean collapseMultipleCodeLines = false;
        boolean ignoreEmptyLines = true;
        List<String> flags = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
        if (flags.contains("-c") || flags.contains("--collapse-blocks")) {
            collapseMultipleCodeLines = true;
        }
        if (flags.contains("-e") || flags.contains("--with-empty-lines")) {
            ignoreEmptyLines = false;
        }

        TextDiffToTikz me = new TextDiffToTikz(collapseMultipleCodeLines, ignoreEmptyLines);
        if (Files.isDirectory(fileToConvert)) {
            Logger.info("Processing directory " + fileToConvert);
            for (Path file : FileUtils.listAllFilesRecursively(fileToConvert)) {
                if (FileUtils.hasExtension(file, ".diff")) {
                    me.textDiff2Tikz(file, layout);
                }
            }
        } else {
            me.textDiff2Tikz(fileToConvert, layout);
        }
    }

    public void textDiff2Tikz(Path fileToConvert, GraphvizExporter.LayoutAlgorithm layout) throws IOException, DiffParseException {
        Logger.info("Converting file " + fileToConvert);
        Logger.info("Using layout " + layout.getExecutableName());
        final Path targetFile = fileToConvert.resolveSibling(fileToConvert.getFileName() + ".tikz");

        final VariationDiff<DiffLinesLabel> d = VariationDiff.fromFile(fileToConvert, new VariationDiffParseOptions(collapseMultipleCodeLines, ignoreEmptyLines));
        final String tikz = exportAsTikz(d, layout);
        IO.write(targetFile, tikz);
        Logger.info("Wrote file " + targetFile);
    }

    public static String exportAsTikz(final VariationDiff<DiffLinesLabel> variationDiff, GraphvizExporter.LayoutAlgorithm layout) throws IOException {
        // Export the test case
        var tikzOutput = new ByteArrayOutputStream();
        new TikzExporter<>(format).exportVariationDiff(variationDiff, layout, tikzOutput);
        return tikzOutput.toString();
    }

    public static String tikzNodeLabel(DiffNode<? extends DiffLinesLabel> node) {
        if (node.isRoot()) {
            return "r";
        } else {
            if (node.isIf() || node.isElif()) {
                return node.getFormula().toString(UNICODE_PROP_SYMBOLS);
            } else {
                return node.getLabel().getLines().get(0).trim();
            }
//                    .map(String::trim)
//                    .collect(Collectors.joining("<br>"));// substringBefore(node.getLabel(), StringUtils.LINEBREAK_REGEX);
        }
    }

    public static String substringBefore(String str, Pattern end) {
        Matcher m = end.matcher(str);
        if (m.find()) {
            return m.group(0);
        } else {
            return str;
        }
    }
}

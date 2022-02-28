package mining;

import de.variantsync.functjonal.Functjonal;
import metadata.AtomicPatternCount;
import mining.dataset.MiningDataset;
import org.tinylog.Logger;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.IO;
import util.LaTeX;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Computes a total {@link DiffTreeMiningResult} of several {@link DiffTreeMiningResult} outputs.
 */
public class MiningResultAccumulator {
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    private final static Map<String, BiConsumer<DiffTreeMiningResult, String>> CustomEntryParsers = Map.ofEntries(
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.TREEFORMAT),
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.NODEFORMAT),
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.EDGEFORMAT),
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.TASKNAME),
//            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.REPONAME),
            Map.entry(MetadataKeys.REPONAME, (r, v) -> { /* Ignore repository names as they cannot and should not be merged. */ })
    );

    public static Map<String, DiffTreeMiningResult> getAllTotalResultsIn(final Path folderPath) throws IOException {
        // get all files in the directory which are outputs of DiffTreeMiningResult
        final List<Path> paths = Files.walk(folderPath)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(DiffTreeMiner.TOTAL_RESULTS_FILE_NAME))
                .peek(path -> Logger.info("Processing file " + path))
                .toList();

        final Map<String, DiffTreeMiningResult> results = new HashMap<>();
        for (final Path p : paths) {
            results.put(p.getParent().getFileName().toString(), DiffTreeMiningResult.importFrom(p, CustomEntryParsers));
        }
        return results;
    }


    public static String makeReadable(int number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String makeReadable(double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String makeReadable(String number) {
        if (number.isBlank()) {
            return number;
        }

        try {
            return makeReadable(NUMBER_FORMAT.parse(number).doubleValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The actual computation of a total {@link DiffTreeMiningResult} from multiple metadata outputs.
     * 
     * @return Total {@link DiffTreeMiningResult}
     */
    public static DiffTreeMiningResult computeTotalMetadataResult(final Collection<DiffTreeMiningResult> results) {
        return results.stream().collect(DiffTreeMiningResult.IMONOID);
    }

    public static void addTableHeader(final StringBuilder builder, final Object val, final String delim) {
        final String valStr = val.toString();
        if (valStr.length() > 6) {
            builder.append("\\resultTableHeader{").append(val).append("}").append(delim);
        } else {
            builder.append(val).append(delim);
        }
    }

    public static void addTableHeader(final StringBuilder builder, final Object val) {
        addTableHeader(builder, val, LaTeX.TABLE_SEPARATOR);
    }

    public static void addLastTableHeader(final StringBuilder builder, final Object val) {
        addTableHeader(builder, val, LaTeX.TABLE_ENDROW);
    }

    public static void addTableCell(final StringBuilder builder, final Object val) {
        builder.append(val).append(LaTeX.TABLE_SEPARATOR);
    }

    public static void addLastTableCell(final StringBuilder builder, final Object val) {
        builder.append(val).append(LaTeX.TABLE_ENDROW);
    }

    public static String asLaTeXTableLine(final String name, final String domain, final DiffTreeMiningResult result) {
        final StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append("  ");

        addTableCell(lineBuilder, name);
        addTableCell(lineBuilder, domain);
        addTableCell(lineBuilder, makeReadable(result.totalCommits));
        addTableCell(lineBuilder, makeReadable(result.exportedCommits));
        addTableCell(lineBuilder, makeReadable(result.exportedTrees));

        final LinkedHashMap<AtomicPattern, AtomicPatternCount.Occurrences> apc = result.atomicPatternCounts.getOccurences();
        for (final AtomicPatternCount.Occurrences apo : apc.values()) {
            addTableCell(lineBuilder, makeReadable(apo.getTotalAmount()));
//            addTableCell(lineBuilder, apo.getAmountOfUniqueCommits());
        }

        addLastTableCell(lineBuilder, makeReadable(result.runtimeInSeconds));

//                    .append(LaTeX.TABLE_ENDROW);
        return lineBuilder.toString();
    }

    public static String asLaTeXTable(final Map<MiningDataset, DiffTreeMiningResult> datasets, final DiffTreeMiningResult ultimateResult) throws ParseException {
        final StringBuilder table = new StringBuilder();
        final StringBuilder tableHead = new StringBuilder();
        final String indent = "  ";

        table.append("\\begin{tabular}{l l r | r r ");
        addTableHeader(tableHead, "Name");
        addTableHeader(tableHead, "Domain");
        addTableHeader(tableHead, "\\#total commits");
        addTableHeader(tableHead, "\\#processed commits");
        addTableHeader(tableHead, "\\#mined tree diffs");
        for (final AtomicPattern a : ProposedAtomicPatterns.Instance.all()) {
            addTableHeader(tableHead, a.getName());
            table.append("r ");
        }
        addLastTableHeader(tableHead, "runtime (s)");
        table.append("r}").append(StringUtils.LINEBREAK);

        table.append(tableHead);
        table.append(indent).append("\\hline").append(StringUtils.LINEBREAK);
        final List<String> datasetLines = new ArrayList<>();

        for (final Map.Entry<MiningDataset, DiffTreeMiningResult> datasetEntry : datasets.entrySet()) {
            final MiningDataset dataset = datasetEntry.getKey();
            final DiffTreeMiningResult result = datasetEntry.getValue();
            datasetLines.add(asLaTeXTableLine(dataset.name(), dataset.domain(), result));
        }

        datasetLines.sort(String::compareToIgnoreCase);
        for (final String datasetLine : datasetLines) {
            table.append(datasetLine);
        }

        table.append(indent).append("\\hline").append(StringUtils.LINEBREAK);
        table.append(indent).append("\\hline").append(StringUtils.LINEBREAK);

        table.append(asLaTeXTableLine("total", "--", ultimateResult));
        table.append("\\end{tabular}").append(StringUtils.LINEBREAK);

        return table.toString();
    }

    public static void main(final String[] args) throws IOException, ParseException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected path to input directory but got no arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        final Path latexTablePath = Path.of(args[1]);
        if (!Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Expected path to directory but the given path is not a directory!");
        }

        final Map<String, DiffTreeMiningResult> allResults = getAllTotalResultsIn(inputPath);
        final DiffTreeMiningResult ultimateResult = computeTotalMetadataResult(allResults.values());
        DiffTreeMiner.exportMetadataToFile(inputPath.resolve("ultimateresult" + DiffTreeMiningResult.EXTENSION), ultimateResult);

        final Map<String, MiningDataset> datasetByName;
        try {
            datasetByName = MiningDataset.fromMarkdown(DiffTreeMiner.DATASETS_FILE).stream().collect(Collectors.toMap(
                    MiningDataset::name,
                    Function.identity()
            ));
        } catch (IOException e) {
            Logger.error("Failed to load at least one dataset from " + DiffTreeMiner.DATASETS_FILE + " because:", e);
            Logger.error("Aborting execution!");
            return;
        }

        final Map<MiningDataset, DiffTreeMiningResult> datasetsWithResults = Functjonal.bimap(
                allResults,
                datasetName -> {
                    final MiningDataset dataset = datasetByName.get(datasetName);
                    if (dataset == null) {
                        throw new RuntimeException("Could not find dataset for " + datasetName);
                    }
                    return dataset;
                },
                Function.identity()
        );

        final String latexTable = asLaTeXTable(datasetsWithResults, ultimateResult);
        Logger.info("Results Table:\n" + latexTable);
        IO.write(latexTablePath, latexTable);
    }
}

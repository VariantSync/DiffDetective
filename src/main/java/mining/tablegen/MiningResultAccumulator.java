package mining.tablegen;

import mining.DiffTreeMiner;
import mining.DiffTreeMiningResult;
import mining.MetadataKeys;
import mining.dataset.MiningDataset;
import mining.tablegen.styles.Table1;
import org.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Computes a total {@link DiffTreeMiningResult} of several {@link DiffTreeMiningResult} outputs.
 */
public class MiningResultAccumulator {

    private final static Map<String, BiConsumer<DiffTreeMiningResult, String>> CustomEntryParsers = Map.ofEntries(
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.TREEFORMAT),
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.NODEFORMAT),
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.EDGEFORMAT),
            DiffTreeMiningResult.storeAsCustomInfo(MetadataKeys.TASKNAME)
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


    /**
     * The actual computation of a total {@link DiffTreeMiningResult} from multiple metadata outputs.
     * 
     * @return Total {@link DiffTreeMiningResult}
     */
    public static DiffTreeMiningResult computeTotalMetadataResult(final Collection<DiffTreeMiningResult> results) {
        return results.stream().collect(DiffTreeMiningResult.IMONOID);
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

        final List<ContentRow> datasetsWithResults = allResults.entrySet().stream().map(
                entry -> {
                    final MiningDataset dataset = datasetByName.get(entry.getKey());
                    if (dataset == null) {
                        throw new RuntimeException("Could not find dataset for " + entry.getKey());
                    }
                    return new ContentRow(dataset, entry.getValue());
                }
        ).toList();

        final ContentRow ultimateRow = new ContentRow(
                new MiningDataset(
                        "total",
                        "",
                        "--",
                        ultimateResult.totalCommits + ""
                ),
                ultimateResult
        );

        final TableDefinition tableDef = new Table1();
        final String latexTable = new TableGenerator(tableDef).generateTable(datasetsWithResults, ultimateRow);
        Logger.info("Results Table:\n" + latexTable);
        IO.write(latexTablePath, latexTable);
    }
}

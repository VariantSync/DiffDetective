package org.variantsync.diffdetective.tablegen;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.analysis.AutomationResult;
import org.variantsync.diffdetective.analysis.HistoryAnalysis;
import org.variantsync.diffdetective.analysis.MetadataKeys;
import org.variantsync.diffdetective.datasets.DatasetDescription;
import org.variantsync.diffdetective.main.FindMedianCommitTime;
import org.variantsync.diffdetective.mining.DiffTreeMiner;
import org.variantsync.diffdetective.tablegen.rows.ContentRow;
import org.variantsync.diffdetective.tablegen.styles.ShortTable;
import org.variantsync.diffdetective.tablegen.styles.VariabilityShare;
import org.variantsync.diffdetective.util.IO;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Computes a total {@link AnalysisResult} of several {@link AnalysisResult} outputs.
 */
public class MiningResultAccumulator {
    private final static Map<String, BiConsumer<AnalysisResult, String>> CustomEntryParsers = Map.ofEntries(
            AnalysisResult.storeAsCustomInfo(MetadataKeys.TREEFORMAT),
            AnalysisResult.storeAsCustomInfo(MetadataKeys.NODEFORMAT),
            AnalysisResult.storeAsCustomInfo(MetadataKeys.EDGEFORMAT),
            AnalysisResult.storeAsCustomInfo(MetadataKeys.TASKNAME)
    );

    public static Map<String, AnalysisResult> getAllTotalResultsIn(final Path folderPath) throws IOException {
        // get all files in the directory which are outputs of DiffTreeMiningResult
        final List<Path> paths = Files.walk(folderPath)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(HistoryAnalysis.TOTAL_RESULTS_FILE_NAME))
                .peek(path -> Logger.info("Processing file " + path))
                .toList();

        final Map<String, AnalysisResult> results = new HashMap<>();
        for (final Path p : paths) {
            results.put(p.getParent().getFileName().toString(), AnalysisResult.importFrom(p, CustomEntryParsers));
        }
        return results;
    }


    /**
     * The actual computation of a total {@link AnalysisResult} from multiple metadata outputs.
     * 
     * @return Total {@link AnalysisResult}
     */
    public static AnalysisResult computeTotalMetadataResult(final Collection<AnalysisResult> results) {
        return results.stream().collect(AnalysisResult.IMONOID);
    }

    public static void main(final String[] args) throws IOException, ParseException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected path to input directory but got no arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        final Path latexTableDir = Path.of(args[1]);
        if (!Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Expected path to directory but the given path is not a directory!");
        }

        final Map<String, AnalysisResult> allResults = getAllTotalResultsIn(inputPath);
        final AnalysisResult ultimateResult = computeTotalMetadataResult(allResults.values());
        HistoryAnalysis.exportMetadataToFile(inputPath.resolve("ultimateresult" + AnalysisResult.EXTENSION), ultimateResult);

        final Map<String, DatasetDescription> datasetByName;
        try {
            datasetByName = DatasetDescription.fromMarkdown(DiffTreeMiner.DATASETS_FILE).stream().collect(Collectors.toMap(
                    DatasetDescription::name,
                    Function.identity()
            ));
        } catch (IOException e) {
            Logger.error("Failed to load at least one dataset from " + DiffTreeMiner.DATASETS_FILE + " because:", e);
            Logger.error("Aborting execution!");
            return;
        }

        final List<ContentRow> datasetsWithResults = allResults.entrySet().stream().map(
                entry -> {
                    final AnalysisResult result = entry.getValue();
                    final DatasetDescription dataset = datasetByName.get(entry.getKey());
                    if (dataset == null) {
                        throw new RuntimeException("Could not find dataset for " + entry.getKey());
                    }

                    final AutomationResult automationResult;
                    final Path automationResultDir = inputPath.resolve(dataset.name());
                    try {
                         automationResult = FindMedianCommitTime.getResultOfDirectory(automationResultDir, result.exportedCommits);
                    } catch (IOException e) {
                        Logger.error("Could not load automation results for dataset " + dataset.name() + " in " + automationResultDir);
                        System.exit(0);
                        return null;
                    }

                    return new ContentRow(dataset, result, automationResult);
                }
        ).toList();

        final ContentRow ultimateRow = new ContentRow(
                new DatasetDescription(
                        "total",
                        "",
                        "--",
                        ultimateResult.totalCommits + ""
                ),
                ultimateResult,
                FindMedianCommitTime.getResultOfDirectory(inputPath, FindMedianCommitTime.NUM_EXPECTED_COMMITS)
        );

        for (boolean filtered : List.of(true, false)) {
            for (final boolean absolute : List.of(true, false)) {
                final Supplier<TableDefinition> tableDefFactory = absolute
                        ? (() -> ShortTable.Absolute(filtered))
                        : (() -> ShortTable.Relative(filtered));
                String prefix = filtered ? "filtered_" : "all_";
                prefix += absolute ? "absolute_" : "relative_";

                final String ultimateResultsTable = new TableGenerator(tableDefFactory.get()).generateTable(datasetsWithResults, ultimateRow);
                Logger.info("Results Table:\n" + ultimateResultsTable);
                IO.write(latexTableDir.resolve(prefix + "ultimateresults.tex"), ultimateResultsTable);

                final String variabilityTable = new TableGenerator(new VariabilityShare(tableDefFactory)).generateTable(datasetsWithResults, ultimateRow);
                Logger.info("Results Table:\n" + variabilityTable);
                IO.write(latexTableDir.resolve(prefix + "variability.tex"), variabilityTable);
            }
        }

    }
}

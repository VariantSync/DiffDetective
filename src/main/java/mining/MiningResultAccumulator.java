package mining;

import de.variantsync.functjonal.Result;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Computes a total {@link DiffTreeMiningResult} of several {@link DiffTreeMiningResult} outputs.
 */
public class MiningResultAccumulator {
    private final static Map<String, BiConsumer<DiffTreeMiningResult, String>> CustomEntryParsers = Map.ofEntries(
            DiffTreeMiningResult.storeAsCustomInfo("treeformat"),
            DiffTreeMiningResult.storeAsCustomInfo("nodeformat"),
            DiffTreeMiningResult.storeAsCustomInfo("edgeformat")
    );

    /**
     * The actual computation of a total {@link DiffTreeMiningResult} from multiple metadata outputs.
     * 
     * @return Total {@link DiffTreeMiningResult}
     */
    public static Result<DiffTreeMiningResult, IOException> computeTotalMetadataResult(final Path folderPath) throws IOException {
        final Stream<Path> paths = Files.walk(folderPath);
        
        // get all files in the directory which are outputs of DiffTreeMiningResult
        return paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(DiffTreeMiner.TOTAL_RESULTS_FILE_NAME))
                .peek(path -> Logger.info("Processing file " + path))
                .map(Result.Try(path -> DiffTreeMiningResult.importFrom(path, CustomEntryParsers)))
                .collect(Result.MONOID(DiffTreeMiningResult.IMONOID, (a, b) -> a));
    }

    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected path to input directory but got no arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        if (!Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Expected path to directory but the given path is not a directory!");
        }

        computeTotalMetadataResult(inputPath).peek(
                metadata -> DiffTreeMiner.exportMetadataToFile(inputPath.resolve("ultimateresult" + DiffTreeMiningResult.EXTENSION), metadata),
                Logger::error
        );
    }
}

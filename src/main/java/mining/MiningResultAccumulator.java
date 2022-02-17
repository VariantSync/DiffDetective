package mining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Computes a total {@link DiffTreeMiningResult} of several {@link DiffTreeMiningResult} outputs.
 */
public class MiningResultAccumulator {

    /**
     * The actual computation of a total {@link DiffTreeMiningResult} from multiple metadata outputs.
     * 
     * @return Total {@link DiffTreeMiningResult}
     */
    public static DiffTreeMiningResult computeTotalMetadataResult(final Path folderPath) throws IOException {
        
        Stream<Path> paths = Files.walk(folderPath);
        List<Path> pathsOfTotalMetadataFiles = new ArrayList<>();
        
        // get all files in the directory which are outputs of DiffTreeMiningResult
        paths
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().toLowerCase().endsWith(DiffTreeMiner.TOTAL_RESULTS_FILE_NAME))
            .forEachOrdered(pathsOfTotalMetadataFiles::add);
        
        final DiffTreeMiningResult totalResult = new DiffTreeMiningResult();
        // join all metadata files into one
        for (final Path p : pathsOfTotalMetadataFiles) {
            // parse metadata file and append all info to the total result
            totalResult.append(DiffTreeMiningResult.importFrom(p));
        }
        
        paths.close();
        
        return totalResult;
    }

}

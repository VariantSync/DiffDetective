package mining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	public static DiffTreeMiningResult computeTotalMetadataResult() {

		// path to the directory where all metadata files are located in
		final String folderPath = "results/difftrees/";
		
		try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
			List<Path> pathsOfTotalMetadataFiles = new ArrayList<Path>();
			
			// get all files in the directory which are outputs of DiffTreeMiningResult
			paths
				.filter(Files::isRegularFile)
				.filter(p -> p.toString().toLowerCase().endsWith("totalresult.metadata.txt"))
				.forEachOrdered(p -> pathsOfTotalMetadataFiles.add(p));

			final DiffTreeMiningResult totalResult = new DiffTreeMiningResult();
			// join all metadata files into one
			for (final Path p : pathsOfTotalMetadataFiles) {
					// parse metadata file and append all info to the total result
					totalResult.append(DiffTreeMiningResult.importFrom(p));
				}

			return totalResult;

		} catch (IOException e) {
			System.err.println("An IO error occured.");
			e.printStackTrace();
		}
		
		
		return null; // TODO in case of an error, what to do?
	}
	
}

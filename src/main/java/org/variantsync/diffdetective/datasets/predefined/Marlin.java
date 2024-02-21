package org.variantsync.diffdetective.datasets.predefined;

import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.feature.PreprocessorAnnotationParser;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;

import java.nio.file.Path;

/**
 * Default repository for Marlin.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class Marlin {
    public static final PreprocessorAnnotationParser ANNOTATION_PARSER =
            PreprocessorAnnotationParser.CreateCppAnnotationParser(
                    PropositionalFormulaParser.Default,
                    new MarlinCPPDiffLineFormulaExtractor()
            );

    /**
     * Clones Marlin from Github.
     *
     * @param localDir Directory to clone the repository to.
     * @return Marlin repository
     */
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/MarlinFirmware/Marlin.git", "Marlin")
                .orElseThrow()
                .setDiffFilter(StanciulescuMarlin.DIFF_FILTER)
                .setParseOptions(PatchDiffParseOptions.Default.withAnnotationParser(ANNOTATION_PARSER));
    }
}

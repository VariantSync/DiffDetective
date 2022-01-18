package datasets.predefined;

import datasets.ParseOptions;
import datasets.Repository;
import diff.difftree.parse.DiffNodeParser;
import feature.CPPAnnotationParser;
import feature.PropositionalFormulaParser;

import java.nio.file.Path;

/**
 * Default repository for Marlin.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class Marlin {
    public static final DiffNodeParser ANNOTATION_PARSER = new DiffNodeParser(
            new CPPAnnotationParser(PropositionalFormulaParser.Default, new MarlinCPPDiffLineFormulaExtractor())
    );

    /**
     * Clones Marlin from Github.
     * @param localDir Directory to clone the repository to.
     * @return Marlin repository
     */
    public static Repository cloneFromGithubTo(Path localDir) {
        return Repository
                .tryFromRemote(localDir, "https://github.com/MarlinFirmware/Marlin.git", "Marlin")
                .orElseThrow()
                .setDiffFilter(StanciulescuMarlin.DIFF_FILTER)
                .setParseOptions(new ParseOptions(ANNOTATION_PARSER));
    }
}

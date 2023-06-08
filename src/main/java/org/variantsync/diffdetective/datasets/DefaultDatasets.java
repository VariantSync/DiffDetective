package org.variantsync.diffdetective.datasets;

import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to load descriptions of the 44 open-source software product line repositories.
 * @author Paul Bittner
 */
public class DefaultDatasets {
    /**
     * Path to the markdown file with the links and metadata for each default dataset.
     */
    public final static Path DEFAULT_DATASETS_FILE = Path.of("docs", "verification", "datasets.md");

    /**
     * Path to the
     */
    public final static Path TESTING_DATASETS_FILE = Path.of("docs", "paper_evaluation", "datasets.md");

    /**
     * Path to the markdown file with the links and metadata for Emacs only.
     */
    public final static Path EMACS = Path.of("docs", "datasets", "emacs.md");

    /**
     * Loads the default datasets file and returns a list with a description for each dataset.
     */
    public static List<DatasetDescription> loadDefaultDatasets() {
        return loadDatasets(DEFAULT_DATASETS_FILE);
    }

    /**
     * Parses the given datasets markdown file and returns a list with a description for each dataset described
     * in the fiven file.
     * @param datasetsFile Markdown file with a table in which rows are datasets.
     */
    public static List<DatasetDescription> loadDatasets(final Path datasetsFile) {
        try {
            return DatasetDescription.fromMarkdown(datasetsFile);
        } catch (IOException e) {
            Logger.error(e, "Failed to load at least one dataset from {} because", datasetsFile);
            Logger.error("Aborting execution!");
            return new ArrayList<>();
        }
    }
}

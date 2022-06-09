package org.variantsync.diffdetective.datasets;

import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DefaultDatasets {
    public final static Path DEFAULT_DATASETS_FILE = Path.of("docs", "datasets.md");
    public final static Path EMACS = Path.of("docs", "datasets", "emacs.md");

    public static List<DatasetDescription> loadDefaultDatasets() {
        return loadDatasets(DEFAULT_DATASETS_FILE);
    }

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

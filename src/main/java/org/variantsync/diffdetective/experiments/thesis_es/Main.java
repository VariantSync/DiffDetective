package org.variantsync.diffdetective.experiments.thesis_es;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.DiffFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;

public class Main {

    public static String dataSetPath = "";

    public static void main(String[] args) throws IOException {
        startAnalysis();

    }

    private static void startAnalysis() throws IOException {
        final AnalysisRunner.Options analysisOptions = new AnalysisRunner.Options(
                Paths.get("..", "DiffDetectiveReplicationDatasets"),
                Paths.get("results", "thesis_es"),
                Paths.get("docs", "datasets", "eugen-bachelor-thesis.md"),
                repo -> new PatchDiffParseOptions(
                        PatchDiffParseOptions.DiffStoragePolicy.DO_NOT_REMEMBER,
                        VariationDiffParseOptions.Default),
                repo -> new DiffFilter.Builder().allowMerge(true)
                        .allowedFileExtensions("c", "cpp").build(),
                true,
                false);

        AnalysisRunner.run(analysisOptions, extractionRunner());
    }

    protected static BiConsumer<Repository, Path> extractionRunner() {
        return (repo, repoOutputDir) -> {

            final BiFunction<Repository, Path, Analysis> AnalysisFactory = (r, out) -> new Analysis("Thesis Eugen Shulimov",
                    List.of(new UnparseAnalysis()), r, out);

            Analysis.forEachCommit(() -> AnalysisFactory.apply(repo, repoOutputDir));

        };
    }

}

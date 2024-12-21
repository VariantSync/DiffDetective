package org.variantsync.diffdetective.experiments.thesis_es;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.variantsync.diffdetective.AnalysisRunner;
import org.variantsync.diffdetective.analysis.Analysis;
import org.variantsync.diffdetective.datasets.DatasetDescription;
import org.variantsync.diffdetective.datasets.DefaultDatasets;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions.DiffStoragePolicy;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.DiffFilter;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;

public class Main {

    public static void main(String[] args) throws IOException {
        startAnalysis();
        evaluationAnalysis(Path.of("docs", "datasets", "eugen-bachelor-thesis.md"));

    }

    private static void startAnalysis() throws IOException {
        final AnalysisRunner.Options analysisOptions = new AnalysisRunner.Options(
                Paths.get("..", "DiffDetectiveReplicationDatasets"),
                Paths.get("results", "thesis_es"),
                Paths.get("docs", "datasets", "eugen-bachelor-thesis.md"),
                repo -> new PatchDiffParseOptions(
                        DiffStoragePolicy.REMEMBER_FULL_DIFF,
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

    /**
     * Verarbeitet die Ergebnisse der Analyse um aus einzelnen
     * Angaben eine gesamt Übersicht zu bekommen
     *
     * @param path Pfad zu der markdown datei, aus der die repositories für die
     *             Analyse stammen
     * @throws IOException
     */
    private static void evaluationAnalysis(Path path) throws IOException {
        int count = 0;
        int[] diffTest = { 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] diffSemEqTest = { 0, 0, 0, 0, };
        int[] treeTest = { 0, 0, 0, 0, 0, 0, 0, 0 };
        final List<DatasetDescription> datasets = DefaultDatasets.loadDatasets(path);

        for (DatasetDescription description : datasets) {
            Stream<Path> files = Files
                    .list(Path.of("results", "thesis_es", description.name()))
                    .filter(filename -> filename.getFileName().toString().endsWith(".thesis_es.csv"));
            for (Path tempPath : files.toList()) {
                String[] splitFileData = Files.readString(tempPath).split("\n");
                for (int j = 1; j < splitFileData.length; j++) {
                    String[] splitLineData = splitFileData[j].split(";");
                    for (int i = 0; i < splitLineData.length; i++) {
                        splitLineData[i] = parseNumberStringToIntWithLengthGreaterOne(splitLineData[i]);
                    }
                    count = count + 1;
                    diffTest[0] = diffTest[0] + Integer.parseInt(splitLineData[8]);
                    diffTest[1] = diffTest[1] + Integer.parseInt(splitLineData[9]);
                    diffTest[2] = diffTest[2] + Integer.parseInt(splitLineData[10]);
                    diffTest[3] = diffTest[3] + Integer.parseInt(splitLineData[11]);
                    diffTest[4] = diffTest[4] + Integer.parseInt(splitLineData[12]);
                    diffTest[5] = diffTest[5] + Integer.parseInt(splitLineData[13]);
                    diffTest[6] = diffTest[6] + Integer.parseInt(splitLineData[14]);
                    diffTest[7] = diffTest[7] + Integer.parseInt(splitLineData[15]);
                    diffSemEqTest[0] = diffSemEqTest[0] + Integer.parseInt(splitLineData[16]);
                    diffSemEqTest[1] = diffSemEqTest[1] + Integer.parseInt(splitLineData[17]);
                    diffSemEqTest[2] = diffSemEqTest[2] + Integer.parseInt(splitLineData[18]);
                    diffSemEqTest[3] = diffSemEqTest[3] + Integer.parseInt(splitLineData[19]);
                    treeTest[0] = treeTest[0] + Integer.parseInt(splitLineData[20]);
                    treeTest[1] = treeTest[1] + Integer.parseInt(splitLineData[21]);
                    treeTest[2] = treeTest[2] + Integer.parseInt(splitLineData[22]);
                    treeTest[3] = treeTest[3] + Integer.parseInt(splitLineData[23]);
                    treeTest[4] = treeTest[4] + Integer.parseInt(splitLineData[24]);
                    treeTest[5] = treeTest[5] + Integer.parseInt(splitLineData[25]);
                    treeTest[6] = treeTest[6] + Integer.parseInt(splitLineData[26]);
                    treeTest[7] = treeTest[7] + Integer.parseInt(splitLineData[27]);
                    treeTest[0] = treeTest[0] + Integer.parseInt(splitLineData[28]);
                    treeTest[1] = treeTest[1] + Integer.parseInt(splitLineData[29]);
                    treeTest[2] = treeTest[2] + Integer.parseInt(splitLineData[30]);
                    treeTest[3] = treeTest[3] + Integer.parseInt(splitLineData[31]);
                    treeTest[4] = treeTest[4] + Integer.parseInt(splitLineData[32]);
                    treeTest[5] = treeTest[5] + Integer.parseInt(splitLineData[33]);
                    treeTest[6] = treeTest[6] + Integer.parseInt(splitLineData[34]);
                    treeTest[7] = treeTest[7] + Integer.parseInt(splitLineData[35]);

                }
            }
        }
        List<String> result = new ArrayList<>();
        result.add("Anzahl geprüfter Diffs : " + count + "\n");
        result.add("Anzahl syntaktisch korrekter Diffs mit MultiLine0 und EmptyLine0 : " + diffTest[0] + "\n");
        result.add("Anzahl syntaktisch korrekter Diffs ohne Whitespace mit MultiLine0 und EmptyLine0 : " + diffTest[4]
                + "\n");
        result.add("Anzahl semantisch korrekter Diffs mit MultiLine0 und EmptyLine0 : " + diffSemEqTest[0] + "\n");
        result.add("Anzahl von Diffs mit MultiLine0 und EmptyLine0, welche keine Korrektheitskriterium erfühlt haben : "
                + (count - diffSemEqTest[0]) + "\n");

        result.add("Anzahl syntaktisch korrekter Diffs mit MultiLine1 und EmptyLine0 : " + diffTest[1] + "\n");
        result.add("Anzahl syntaktisch korrekter Diffs ohne Whitespace mit MultiLine1 und EmptyLine0 : " + diffTest[5]
                + "\n");
        result.add("Anzahl semantisch korrekter Diffs mit MultiLine1 und EmptyLine0 : " + diffSemEqTest[1] + "\n");
        result.add("Anzahl von Diffs mit MultiLine1 und EmptyLine0, welche keine Korrektheitskriterium erfühlt haben : "
                + (count - diffSemEqTest[1]) + "\n");

        result.add("Anzahl syntaktisch korrekter Diffs mit MultiLine0 und EmptyLine1 : " + diffTest[2] + "\n");
        result.add("Anzahl syntaktisch korrekter Diffs ohne Whitespace mit MultiLine0 und EmptyLine1 : " + diffTest[6]
                + "\n");
        result.add("Anzahl semantisch korrekter Diffs mit MultiLine0 und EmptyLine1 : " + diffSemEqTest[2] + "\n");
        result.add("Anzahl von Diffs mit MultiLine0 und EmptyLine1, welche keine Korrektheitskriterium erfühlt haben : "
                + (count - diffSemEqTest[2]) + "\n");

        result.add("Anzahl syntaktisch korrekter Diffs mit MultiLine1 und EmptyLine1 : " + diffTest[3] + "\n");
        result.add("Anzahl syntaktisch korrekter Diffs ohne Whitespace mit MultiLine1 und EmptyLine1 : " + diffTest[7]
                + "\n");
        result.add("Anzahl semantisch korrekter Diffs mit MultiLine1 und EmptyLine1 : " + diffSemEqTest[3] + "\n");
        result.add("Anzahl von Diffs mit MultiLine1 und EmptyLine1, welche keine Korrektheitskriterium erfühlt haben : "
                + (count - diffSemEqTest[3]) + "\n");

        result.add("-------------------------------------------------------------------------------------------");
        result.add("Anzahl geprüfter Trees : " + count * 2 + "\n");
        result.add("Anzahl syntaktisch korrekter Trees mit MultiLine0 und EmptyLine0 : " + treeTest[0] + "\n");
        result.add("Anzahl syntaktisch korrekter Trees ohne Whitespace mit MultiLine0 und EmptyLine0 : " + treeTest[4]
                + "\n");
        result.add("Anzahl von Trees mit MultiLine0 und EmptyLine0, welche keine Korrektheitskriterium erfühlt haben : "
                + (2 * count - treeTest[4]) + "\n");

        result.add("Anzahl syntaktisch korrekter Trees mit MultiLine1 und EmptyLine0 : " + treeTest[1] + "\n");
        result.add("Anzahl syntaktisch korrekter Trees ohne Whitespace mit MultiLine1 und EmptyLine0 : " + treeTest[5]
                + "\n");
        result.add("Anzahl von Trees mit MultiLine1 und EmptyLine0, welche keine Korrektheitskriterium erfühlt haben : "
                + (2 * count - treeTest[5]) + "\n");

        result.add("Anzahl syntaktisch korrekter Trees mit MultiLine0 und EmptyLine1 : " + treeTest[2] + "\n");
        result.add("Anzahl syntaktisch korrekter Trees ohne Whitespace mit MultiLine0 und EmptyLine1 : " + treeTest[6]
                + "\n");
        result.add("Anzahl von Trees mit MultiLine0 und EmptyLine1, welche keine Korrektheitskriterium erfühlt haben : "
                + (2 * count - treeTest[6]) + "\n");

        result.add("Anzahl syntaktisch korrekter Trees mit MultiLine1 und EmptyLine1 : " + treeTest[3] + "\n");
        result.add("Anzahl syntaktisch korrekter Trees ohne Whitespace mit MultiLine1 und EmptyLine1 : " + treeTest[7]
                + "\n");
        result.add("Anzahl von Trees mit MultiLine1 und EmptyLine1, welche keine Korrektheitskriterium erfühlt haben : "
                + (2 * count - treeTest[7]) + "\n");

        Files.write(Path.of("results", "thesis_es", "resultOfAnalysis.txt"), result);

    }

    private static String parseNumberStringToIntWithLengthGreaterOne(String string) {
        string = string.trim();
        for (char c : string.toCharArray()) {
            if (Character.isDigit(c)) {
                return Character.toString(c);
            }
        }
        return "";
    }
}

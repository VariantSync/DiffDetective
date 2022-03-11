package mining;

import mining.strategies.CommitHistoryAnalysisTask;
import mining.strategies.CommitProcessTime;
import org.tinylog.Logger;
import util.IO;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FindMedianCommitTime {
    public static final int NUM_EXPECTED_COMMITS = 1_708_181;

    public static final Path outputFile = Path.of("speedstatistics.txt");

    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected path to input directory but got no arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        if (!Files.isDirectory(inputPath)) {
            throw new IllegalArgumentException("Expected path to directory but the given path is not a directory!");
        }

        final List<Path> paths = Files.walk(inputPath)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(CommitHistoryAnalysisTask.COMMIT_TIME_FILE_EXTENSION))
                .peek(path -> Logger.info("Processing file " + path))
                .toList();

        final ArrayList<CommitProcessTime> alltimes = new ArrayList<>(NUM_EXPECTED_COMMITS);

        double sum = 0;
        for (final Path p : paths) {
            sum += parse(p, alltimes);
        }

        final CommitProcessTime[] alltimesArray = alltimes.toArray(CommitProcessTime[]::new);
        Arrays.parallelSort(alltimesArray, Comparator.comparingDouble(CommitProcessTime::milliseconds));

        final StringBuilder results = new StringBuilder();
        results.append("#: " + alltimesArray.length).append(StringUtils.LINEBREAK);
        results.append("Total   commit process time is: " + ((sum / 1000.0) / 60.0) + "min").append(StringUtils.LINEBREAK);
        results.append("Fastest commit process time is: " + alltimesArray[0]).append(StringUtils.LINEBREAK);
        results.append("Slowest commit process time is: " + alltimesArray[alltimesArray.length - 1]).append(StringUtils.LINEBREAK);
        results.append("Median  commit process time is: " + alltimesArray[alltimesArray.length / 2]).append(StringUtils.LINEBREAK);
        results.append("Average commit process time is: " + (sum / ((double) alltimesArray.length)) + "ms").append(StringUtils.LINEBREAK);

        final String resultsStr = results.toString();
        Logger.info("Results:\n" + resultsStr);
        IO.write(inputPath.resolve(outputFile), resultsStr);
    }

    private static double parse(final Path file, final List<CommitProcessTime> times) throws IOException {
        final String fileInput = IO.readAsString(file);
        final String[] lines = fileInput.split(StringUtils.LINEBREAK_REGEX);
        double sumSeconds = 0;

        for (final String line : lines) {
            if (!line.isBlank()) {
                final CommitProcessTime lineTime = CommitProcessTime.fromString(line);
                sumSeconds += lineTime.milliseconds();
                times.add(lineTime);
            } else {
                Logger.warn("Found blank line \"" + line + "\" in " + file);
            }
        }

        return sumSeconds;
    }
}

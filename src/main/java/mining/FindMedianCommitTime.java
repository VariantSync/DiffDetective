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

        for (final Path p : paths) {
            parse(p, alltimes);
        }

        final CommitProcessTime[] alltimesArray = alltimes.toArray(CommitProcessTime[]::new);
        Arrays.parallelSort(alltimesArray, Comparator.comparingDouble(CommitProcessTime::milliseconds));
        Logger.info("Median commit process time is: " + alltimesArray[alltimesArray.length / 2]);
    }

    private static void parse(final Path file, final List<CommitProcessTime> times) throws IOException {
        final String fileInput = IO.readAsString(file);
        final String[] lines = fileInput.split(StringUtils.LINEBREAK_REGEX);

        for (final String line : lines) {
            times.add(CommitProcessTime.fromString(line));
        }
    }
}

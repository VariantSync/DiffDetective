package org.variantsync.diffdetective.main;

import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AutomationResult;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask;
import org.variantsync.diffdetective.analysis.CommitProcessTime;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FindMedianCommitTime {
    public static final int NUM_EXPECTED_COMMITS = 1_708_172;

    public static final Path outputFile = Path.of("speedstatistics.txt");

    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected path to input directory but got no arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        final AutomationResult automationResult = getResultOfDirectory(inputPath, NUM_EXPECTED_COMMITS);

        final String resultsStr = automationResult.exportTo(inputPath.resolve(outputFile));
        Logger.info("Results:{}", resultsStr);

//        Logger.info("info");
//        Logger.warn("warn");
//        Logger.error("error");
//        Logger.debug("debug");
//        Logger.trace("trace");
    }

    public static AutomationResult getResultOfDirectory(final Path directory, int numExpectedCommits) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Expected path to directory but the given path is not a directory!");
        }

        final List<Path> paths = Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(p -> FileUtils.hasExtension(p, CommitHistoryAnalysisTask.COMMIT_TIME_FILE_EXTENSION))
//                .peek(path -> Logger.info("Processing file {}", path))
                .toList();

        final ArrayList<CommitProcessTime> alltimes = new ArrayList<>(numExpectedCommits);

        long totalTimeMS = 0;
        for (final Path p : paths) {
            totalTimeMS += parse(p, alltimes);
        }

        final CommitProcessTime[] alltimesArray = alltimes.toArray(CommitProcessTime[]::new);
        Arrays.parallelSort(alltimesArray, Comparator.comparingDouble(CommitProcessTime::milliseconds));
        final int numTotalCommits = alltimesArray.length;

        final AutomationResult automationResult;
        if (numTotalCommits == 0) {
            final String repoName = directory.getFileName().toString();
            automationResult = new AutomationResult(
                    numTotalCommits,
                    totalTimeMS,
                    CommitProcessTime.Invalid(repoName),
                    CommitProcessTime.Invalid(repoName),
                    CommitProcessTime.Invalid(repoName)
            );
        } else {
            automationResult = new AutomationResult(
                    numTotalCommits,
                    totalTimeMS,
                    alltimesArray[0],
                    alltimesArray[alltimesArray.length - 1],
                    alltimesArray[alltimesArray.length / 2]
            );
        }

        if (automationResult.numMeasuredCommits() != numExpectedCommits) {
            Logger.error("Expected {} commits but got {}! {} commits are missing!",
                    numExpectedCommits,
                    automationResult.numMeasuredCommits(),
                    (numExpectedCommits - automationResult.numMeasuredCommits()));
        }

        return automationResult;
    }

    private static long parse(final Path file, final List<CommitProcessTime> times) throws IOException {
        final String fileInput = IO.readAsString(file);
        final String[] lines = fileInput.split(StringUtils.LINEBREAK_REGEX);
        long sumSeconds = 0;

        for (final String line : lines) {
            if (!line.isBlank()) {
                final CommitProcessTime lineTime = CommitProcessTime.fromString(line);
                sumSeconds += lineTime.milliseconds();
                times.add(lineTime);
            } else {
                Logger.warn("Found blank line '{}' in {}", line, file);
            }
        }

        return sumSeconds;
    }
}

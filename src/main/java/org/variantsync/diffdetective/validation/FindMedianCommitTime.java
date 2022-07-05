package org.variantsync.diffdetective.validation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AutomationResult;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask;
import org.variantsync.diffdetective.analysis.CommitProcessTime;
import org.variantsync.diffdetective.util.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Program to find the median commit time after the {@link Validation} has been performed.
 * This program will iterate through all commit times reported by the validation, load them,
 * and find average time, median time, the fastest, and the slowest commit.
 * @author Paul Bittner
 */
public class FindMedianCommitTime {
    /**
     * File to write the result to.
     */
    public static final Path outputFile = Path.of("speedstatistics.txt");

    /**
     * Main method. Expects exactly one argument: The path to the root directory of the validation output.
     * @param args An array of size 1, containing the path to validation output is expected.
     * @throws IOException when {@link #getResultOfDirectory} throws.
     */
    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected path to input directory but got no arguments!");
        }

        final Path inputPath = Path.of(args[0]);
        final AutomationResult automationResult = getResultOfDirectory(inputPath);

        final String resultsStr = automationResult.exportTo(inputPath.resolve(outputFile));
        Logger.info("Results:{}", resultsStr);

//        Logger.info("info");
//        Logger.warn("warn");
//        Logger.error("error");
//        Logger.debug("debug");
//        Logger.trace("trace");
    }

    /**
     * Summarizes the commit time results found in the given validation output directory.
     * The directory should point to the root of the directory in which the results of an execution
     * of the {@link Validation} can be found.
     * @param directory Validation output directory.
     * @return Summary of commit process times with various speed statistics.
     * @throws IOException when iterating the files in the given directory fails for some reason.
     */
    public static AutomationResult getResultOfDirectory(final Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Expected path to directory but "+ directory +" is not a directory!");
        }

        // This stream needs {@code O(n log(n))} time (because of the sort) and {@code O(n)} space
        // (because the list has to be captured). This can be improved to {@code O(n)} time and
        // {@code O(1)} space by using the Median of medians algorithm and computing the minimum and
        // maximum like {@code LongSummaryStatistics} does.
        ImmutablePair<Long, List<CommitProcessTime>> result;
        try (Stream<Path> paths = Files.walk(directory)) {
            result = paths
                .parallel()
                .filter(p -> FileUtils.hasExtension(p, CommitHistoryAnalysisTask.COMMIT_TIME_FILE_EXTENSION))
                .filter(Files::isRegularFile)
//                .peek(path -> Logger.info("Processing file {}", path))
                .flatMap(FindMedianCommitTime::parse)
                .sorted(Comparator.comparingDouble(CommitProcessTime::milliseconds))
                .collect(Collectors.teeing(
                            Collectors.summingLong(CommitProcessTime::milliseconds),
                            Collectors.toList(),
                            ImmutablePair::new)
                );
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }

        long totalTimeMS = result.getLeft();
        List<CommitProcessTime> alltimes = result.getRight();

//        if (alltimes.size() != numExpectedCommits) {
//            Logger.error("Expected {} commits but got {}! {} commits are missing!",
//                numExpectedCommits,
//                alltimes.size(),
//                numExpectedCommits - alltimes.size());
//        }

        if (alltimes.size() == 0) {
            final String repoName = directory.getFileName().toString();
            return new AutomationResult(
                    alltimes.size(),
                    totalTimeMS,
                    CommitProcessTime.Invalid(repoName),
                    CommitProcessTime.Invalid(repoName),
                    CommitProcessTime.Invalid(repoName)
            );
        } else {
            return new AutomationResult(
                    alltimes.size(),
                    totalTimeMS,
                    alltimes.get(0),
                    alltimes.get(alltimes.size() - 1),
                    alltimes.get(alltimes.size() / 2)
            );
        }
    }

    /**
     * Parses all CommitProcessTimes in the given file.
     * @param file Path to a commit times file (ending with <code>.committimes.txt</code>).
     * @return A stream of the CommitProcessTimes in the given file.
     */
    private static Stream<CommitProcessTime> parse(final Path file) {
        try {
            // This stream has to be closed by the caller of {@code parse}, because the returned
            // stream is not consumed inside of this method.
            return Files.lines(file)
                .filter(
                    line -> {
                        if (line.isBlank()) {
                            Logger.warn("Found blank line in {}", file);
                            return false;
                        } else {
                            return true;
                        }
                    }
                ).map(CommitProcessTime::fromString);
        } catch (IOException e) {
            // Checked exceptions can't be propagated because the caller of {@code parse} requires
            // a method wich doesn't throw any checked exception.
            throw new UncheckedIOException(e);
        }
    }
}

package org.variantsync.diffdetective.util;

import org.apache.commons.io.FilenameUtils;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Utility functions for handling file paths. */
public class FileUtils {
    /** Replace the line endings of any operating system in {@code text} by {@code replace} */
    public static String replaceLineEndings(final String text, final String replacee) {
        return StringUtils.LINEBREAK_REGEX.matcher(text).replaceAll(replacee);
    }

    /**
     * Returns {@code true} iff {@code p} can be created or is an empty directory.
     *
     * @throws IOException if the file system signals an error
     */
    public static boolean isEmptyDirectory(final Path p) throws IOException {
        return !Files.exists(p) || (Files.isDirectory(p) && Files.list(p).findAny().isEmpty());
    }

    /**
     * Returns {@code true} if {@code p} can be created or is an empty directory.
     * If the file system signals an error, this function returns {@code false}.
     */
    public static boolean tryIsEmptyDirectory(final Path p) {
        try {
            return isEmptyDirectory(p);
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    /**
     * Returns a list of all regular files inside/below {@code directory}.
     *
     * @throws IOException if the file system signals an error
     */
    public static List<Path> listAllFilesRecursively(final Path directory) throws IOException {
        return Files.find(directory,
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> fileAttr.isRegularFile()).toList();
    }

    /**
     * Checks if {@code p} has the file extension {@code expected}.
     * Note that the common dot delimiter has to be included in {@code expected}.
     */
    public static boolean hasExtension(final Path p, final String expected) {
        return p.getFileName().toString().toLowerCase().endsWith(expected.toLowerCase());
    }

    /** Check if the path {@code p} has a line graph extension. */
    public static boolean isLineGraph(final Path p) {
        return hasExtension(p, ".lg");
    }

    /**
     * Adds {@code extension} to the filename of {@code p}.
     * Note that the common dot delimiter has to be included in {@code extension}.
     */
    public static Path addExtension(final Path p, final String extension) {
        return p.resolveSibling(p.getFileName() + extension);
    }
}

package org.variantsync.diffdetective.util;

import org.apache.commons.io.FilenameUtils;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtils {
    public static String normalizedLineEndings(final String text) {
        return text.replaceAll(StringUtils.LINEBREAK_REGEX, StringUtils.LINEBREAK);
    }

    public static boolean isEmptyDirectory(final Path p) throws IOException {
        return !Files.exists(p) || (Files.isDirectory(p) && Files.list(p).findAny().isEmpty());
    }

    public static boolean tryIsEmptyDirectory(final Path p) {
        try {
            return isEmptyDirectory(p);
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    public static List<Path> listAllFilesRecursively(final Path directory) throws IOException {
        return Files.find(directory,
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> fileAttr.isRegularFile()).toList();
    }

    /**
     * Checks if {@code p} has the file extension {@code expected}.
     *
     * Note that the common dot delimiter has to included in {@code expected}.
     */
    public static boolean hasExtension(final Path p, final String expected) {
        return p.getFileName().toString().toLowerCase().endsWith(expected.toLowerCase());
    }

    public static boolean isLineGraph(final Path p) {
        return hasExtension(p, ".lg");
    }

    /**
     * Read a text file.
     *
     * @param path Path to the file to read.
     * @return The content of the file as a utf8 encoded string.
     */
    public static String readUTF8(final Path path) {
        try {
            byte[] encoded = Files.readAllBytes(path);
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Path addExtension(final Path p, final String extension) {
        return p.resolveSibling(p.getFileName() + extension);
    }
}

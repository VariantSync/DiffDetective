package util;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtils {
    public static final String LINEBREAK_REGEX = "\\r?\\n";

    public static String normalizedLineEndings(final String text) {
        return text.replace(FileUtils.LINEBREAK_REGEX, "\\n");
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

    public static boolean hasExtension(final Path p, final String extension) {
        final boolean withDot = extension.startsWith(".");

        final String filename = p.getFileName().toString();
        final String fileextension = filename.substring(
                filename.lastIndexOf(".")
                        + (withDot ? 0 : 1)
        );

        return extension.equalsIgnoreCase(fileextension);
    }

    public static boolean isLineGraph(final Path p) {
        return hasExtension(p, ".lg");
    }
}

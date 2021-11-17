package util;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static boolean isEmptyDirectory(final Path p) throws IOException {
        return !Files.exists(p) || (Files.isDirectory(p) && Files.list(p).findAny().isPresent());
    }

    public static boolean tryIsEmptyDirectory(final Path p) {
        try {
            return isEmptyDirectory(p);
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
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

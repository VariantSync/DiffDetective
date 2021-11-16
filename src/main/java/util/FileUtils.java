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
}

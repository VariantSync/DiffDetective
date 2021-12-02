package util;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtils {
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
}

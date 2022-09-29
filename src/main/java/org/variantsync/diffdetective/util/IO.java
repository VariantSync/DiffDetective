package org.variantsync.diffdetective.util;

import org.tinylog.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Util class for exporting data.
 *
 * @author Sören Viegener
 */
public class IO {
    private static final String CSV_DELIMITER = ",";

    /**
     * Exports data to a csv-file
     * @param fileName Name of the file to export to
     * @param headers Names of the headers of the table
     * @param objects Array of array of objects. The amount of objects has to equal the length of the headers array
     * @throws FileNotFoundException When the fileName is not a valid file path
     */
    public static void exportCsv(String fileName, String[] headers, Object[]... objects) throws FileNotFoundException {
        if (headers.length != objects.length) {
            Logger.warn("Header length and object[] length is not equal while exporting csv file {}",
                    fileName);
        }

        int len = objects[0].length;
        for (Object[] os : objects) {
            if (os.length != len) {
                Logger.warn("All object[] do not have equal length while exporting csv file {}",
                        fileName);
                break;
            }
        }

        PrintWriter writer = new PrintWriter(fileName);
        String header = String.join(CSV_DELIMITER, headers) + "\n";
        writer.write(header);

        for (int i = 0; i < len; i++) {
            StringJoiner sj = new StringJoiner(CSV_DELIMITER);
            for(Object[] o : objects){
                sj.add(o[i].toString());
            }
            writer.write(sj + "\n");
        }

        writer.flush();
        writer.close();
    }

    /**
     * Creates all parent directories of {@code file}.
     */
    public static void createParentDirectories(Path file) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
    }

    /**
     * Same as {@link Files#newOutputStream} but creates all parent directories of
     * {@code file} and wraps the result in a {@link BuferedOutputStream}.
     */
    public static BufferedOutputStream newBufferedOutputStream(Path file, OpenOption... openOptions) throws IOException {
        createParentDirectories(file);

        var outputStream = Files.newOutputStream(file, openOptions);
        try {
            return new BufferedOutputStream(outputStream);
        } catch (Exception e) {
            outputStream.close();
            throw e;
        }
    }

    /**
     * Writes the given text to the given file.
     * Creates a new file and its parent directories if necessary. It assumes that no file exists
     * yet at the given path.
     *
     * @param p file to create and fill with {@code text}
     * @param text text to write to the file
     * @throws IOException if {@code p} already exists, an I/O error occurs writing to or creating
     * the file, or the text cannot be encoded using UTF-8
     */
    public static void write(final Path p, final String text) throws IOException {
        createParentDirectories(p);
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Appends the given text to the given file.
     * Creates a new file and its parent directories if necessary. It the file already exists
     * {@code text} is appended to the file.
     *
     * @param p file to create and fill with {@code text}
     * @param text text to write to the file
     * @throws IOException if an I/O error occurs appending to or creating the file, or the text
     * cannot be encoded using UTF-8
     */
    public static void append(final Path p, final String text) throws IOException {
        createParentDirectories(p);
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Writes {@code data} to the file {@code outputPath}.
     * {@code outputPath} and its parent directories will be created if necessary. If
     * {@code outputPath} already exists, no data is written.
     */
    public static void tryWrite(final Path outputPath, final String data) {
        try {
            IO.write(outputPath, data);
        } catch (IOException exception) {
            Logger.error(exception);
        }
    }

    /**
     * Parses the string {@code uri} into an {@code URI} object.
     *
     * <p>The purpose of this function is to handle parse failures in a functional way (using
     * {@link Optional} instead of with exceptions.
     *
     * @param uri the {@code String} to be parsed as an {@code URI}
     * @return the parsed URI if {@code uri} is valid or an empty optional on parse failures
     */
    public static Optional<URI> tryParseURI(final String uri) {
        URI remote;
        try {
            remote = new URI(uri);
        } catch (URISyntaxException e) {
            Logger.error(e);
            return Optional.empty();
        }
        return Optional.of(remote);
    }

    /**
     * Deletes {@code file} if it exists.
     *
     * @return {@code false} if the file system signals an error
     */
    public static boolean tryDeleteFile(Path file) {
        if (Files.exists(file)) {
            try {
                Files.delete(file);
                return true;
            } catch (IOException e) {
                Logger.error(e);
                return false;
            }
        } else {
            return true;
        }
    }
}

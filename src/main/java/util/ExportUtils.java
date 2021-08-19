package util;

import org.pmw.tinylog.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;

/**
 * Util class for exporting data.
 *
 * @author Sören Viegener
 */
public class ExportUtils {
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
            Logger.warn("Header length and object[] length is not equal while exporting csv file " +
                    "{}", fileName);
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
            writer.write(sj.toString() + "\n");
        }

        writer.flush();
        writer.close();
    }

    /**
     * Writes the given text to the given file.
     * Creates a new file and assumes there exists no file yet at the given path.
     * @param p File to create and fill with text.
     * @param text Text to write to file.
     * @throws IOException if an I/O error occurs writing to or creating the file, or the text cannot be encoded using the specified charset.
     *                     Also throws if the given file already exists.
     */
    public static void write(final Path p, final String text) throws IOException {
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

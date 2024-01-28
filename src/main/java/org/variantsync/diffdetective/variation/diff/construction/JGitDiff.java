package org.variantsync.diffdetective.variation.diff.construction;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.diff.*;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Class which groups functions of parsing variation diffs with JGit.
 * @author Paul Bittner
 */
public final class JGitDiff {
    private final static Pattern NO_NEWLINE_AT_END_OF_FILE = Pattern.compile("\n\\\\ No newline at end of file");
    
    private JGitDiff() {}

    /**
     * Creates a text-based diff from two line-based text inputs.
     * Uses JGit to diff the two files using the specified {@code options}.
     * @param beforeFile Path to text file before the change.
     * @param afterFile Path to text file after the change.
     * @param algorithm Specification of which algorithm to use for diffing with JGit.
     * @return A variation diff comprising the changes.
     * @throws IOException when JGit fails in differencing
     */
    public static String textDiff(
            Path beforeFile,
            Path afterFile,
            DiffAlgorithm.SupportedAlgorithm algorithm
    ) throws IOException {
        try (BufferedReader b = Files.newBufferedReader(beforeFile);
             BufferedReader a = Files.newBufferedReader(afterFile)
        ) {
            return textDiff(IOUtils.toString(b), IOUtils.toString(a), algorithm);
        }
    }
    
    /**
     * Creates a text-based diff from two line-based text inputs.
     * Uses JGit to diff the two files using the specified {@code options}.
     * @param linesBefore State of annotated lines before the change.
     * @param linesAfter State of annotated lines after the change.
     * @param algorithm Specification of which algorithm to use for diffing with JGit.
     * @return A variation diff comprising the changes.
     * @throws IOException when JGit fails in differencing
     */
    public static String textDiff(
            String linesBefore,
            String linesAfter,
            DiffAlgorithm.SupportedAlgorithm algorithm
    ) throws IOException {
        final RawText[] text = new RawText[]{
                new RawText(linesBefore.getBytes()),
                new RawText(linesAfter.getBytes())
        };

        // MYERS or HISTOGRAM
        final DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(algorithm);
        final RawTextComparator comparator = RawTextComparator.DEFAULT;
        final EditList diff = diffAlgorithm.diff(
                comparator,
                text[Time.BEFORE.ordinal()],
                text[Time.AFTER.ordinal()]
        );

        String textDiff;
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        /*
        Using our own formatter without diff headers (paired with a maximum context (?))
        caused the formatter to crash due to index out of bounds exceptions.
        So I guess there is a hidden assumption in the DiffFormatter that expects the header
        to be there.

        As a fix, we also use our own construction of embedding patches into the before file to obtain a full diff.
         */
        // final DiffFormatter formatter = makeFormatterWithoutHeader(os);
        // formatter.setContext(Integer.MAX_VALUE); // FULL DIFF
        final DiffFormatter formatter = new DiffFormatter(os);

        formatter.setDiffAlgorithm(diffAlgorithm);
        formatter.setDiffComparator(comparator);
        formatter.setOldPrefix("");
        formatter.setNewPrefix("");

        formatter.format(
                diff,
                text[Time.BEFORE.ordinal()],
                text[Time.AFTER.ordinal()]);
        formatter.flush();
        textDiff = os.toString(StandardCharsets.UTF_8);
        formatter.close();
        os.close();

        textDiff = GitDiffer.getFullDiff(
                new BufferedReader(new StringReader(linesBefore)),
                new BufferedReader(new StringReader(textDiff))
        );

        textDiff = NO_NEWLINE_AT_END_OF_FILE.matcher(textDiff).replaceAll("");
        //textDiff = HUNK_HEADER_REGEX.matcher(textDiff).replaceAll("");
        
        return textDiff;
    }
    
    /**
     * Creates a variation diff from to line-based text inputs.
     * Expects variability to be implemented via C preprocessor in those lines.
     * Uses JGit to diff the two files using the specified {@code options}, and afterwards, creates the variation diff.
     * Creates a variation diff from to line-based text inputs.
     * First creates a line-based diff with {@link #textDiff(String, String, DiffAlgorithm.SupportedAlgorithm)}
     * and then parses that diff with {@link VariationDiff#fromDiff(String, VariationDiffParseOptions)}.
     * @param linesBefore State of annotated lines before the change.
     * @param linesAfter State of annotated lines after the change.
     * @param algorithm Specification of which algorithm to use for diffing with JGit.
     * @param options various options for parsing
     * @return A variation diff comprising the changes.
     * @throws IOException when JGit fails in differencing
     * @throws DiffParseException when DiffDetective fails in parsing the JGit diff to a variation diff
     */
    public static VariationDiff<DiffLinesLabel> diff(
            String linesBefore,
            String linesAfter,
            DiffAlgorithm.SupportedAlgorithm algorithm,
            VariationDiffParseOptions options
    ) throws IOException, DiffParseException {
        return VariationDiff.fromDiff(textDiff(linesBefore, linesAfter, algorithm), options);
    }
}

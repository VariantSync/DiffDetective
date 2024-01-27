package org.variantsync.diffdetective.variation.diff.construction;

import org.eclipse.jgit.diff.*;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.source.PatchString;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Class which groups functions of parsing variation diffs with JGit.
 * @author Paul Bittner
 */
public final class JGitDiff {
    private JGitDiff() {}
    
    /**
     * Creates a variation diff from to line-based text inputs.
     * Expects variability to be implemented via C preprocessor in those lines.
     * Uses JGit to diff the two files using the specified {@code options}, and afterwards, creates the variation diff.
     * @param linesBefore State of annotated lines before the change.
     * @param linesAfter State of annotated lines before the change.
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

        //textDiff = textDiff.replace("\\ No newline at end of file\n", "");
        //textDiff = HUNK_HEADER_REGEX.matcher(textDiff).replaceAll("");

        final VariationDiff<DiffLinesLabel> d;
        try {
            d = VariationDiffParser.createVariationDiff(textDiff, options);
        } catch (DiffParseException e) {
            Logger.error("""
                            Could not parse diff:
                            
                            {}
                            """,
                    textDiff);
            throw e;
        }
        d.setSource(new PatchString(textDiff));
        return d;
    }
}

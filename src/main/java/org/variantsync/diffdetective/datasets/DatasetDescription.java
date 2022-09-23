package org.variantsync.diffdetective.datasets;

import org.tinylog.Logger;
import org.variantsync.diffdetective.util.LaTeX;
import org.variantsync.diffdetective.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description of a git repository dataset.
 * Such a dataset should refer to a git repository of a C preprocessor based software product line.
 * @param name The name of the dataset that is used to identify it (e.g., "Linux").
 * @param repoURL The url of the remote repository (e.g., the HTTPS url of a github repository).
 * @param domain A description of the datasets domain (i.e., the software's purpose such as "operating system" or "database").
 * @param commits The estimated number of commits in the repository as a string. Can be empty.
 * @author Paul Bittner, Benjamin Moosherr
 */
public record DatasetDescription(
        String name,
        String repoURL,
        String domain,
        String commits
) {
    public static DatasetDescription summary(final String name, final String repoURL) {
        return new DatasetDescription(name, repoURL, "", "");
    }

    /**
     * Loads all dataset descriptions in the given markdown file.
     * This expects the markdown file only be a table with the columns
     * - Project name
     * - Domain
     * - Source code available (**y**es/**n**o)?: This should only be a "y" or "n"
     * - Is it a git repository (**y**es/**n**o)?: This should only be a "y" or "n"
     * - Repository URL
     * - Clone URL
     * - Estimated number of commits
     * The first row is expected to be the header.
     * The second row is expected to be a separator and is skipped on parsing.
     * All further rows are expected to describe one dataset each.
     * @param markdownFile Path to a markdown file containing dataset descriptions.
     * @return All parsed dataset descriptions.
     * @throws IOException If the file could not be read for some reason.
     */
    public static List<DatasetDescription> fromMarkdown(final Path markdownFile) throws IOException {
        try (Stream<String> lines = Files.lines(markdownFile)) {
            return lines
                .skip(2) // Skip header
                .map(line -> line.split("\\|"))
                .filter(cells -> {
                    if (cells.length != 7) {
                        Logger.error("Skipping ill-formed line "
                                + String.join("|", cells)
                                + "! Expected 7 entries in a table (separated by |) but got "
                                + cells.length
                                + "!");
                        return false;
                    }
                    return true;
                })
                .filter(cells ->
                    isYes(cells[2]) && // hasCode
                    isYes(cells[3]) // isGitRepo
                ).map(cells -> new DatasetDescription(
                    cells[0].trim(), // name
                    cells[5].trim(), // clone URL
                    cells[1].trim(), // domain
                    cells[6].trim()) // #commits
                ).collect(Collectors.toList());
        }
    }

    /**
     * Turns the given descriptions into a LaTeX table to include in papers.
     * Each dataset will be a row in the table.
     * @param datasets The datasets to put into the LaTeX table.
     * @return A LaTeX table giving an overview on all datasets.
     */
    public static String asLaTeXTable(final List<DatasetDescription> datasets) {
        final StringBuilder table = new StringBuilder();
        final String indent = "  ";

        table.append("\\begin{tabular}{l l r}").append(StringUtils.LINEBREAK);
        table.append(indent).append("Name").append(LaTeX.TABLE_SEPARATOR).append("Domain").append(LaTeX.TABLE_SEPARATOR).append("\\#commits").append(LaTeX.TABLE_ENDROW);
        table.append(indent).append("\\hline").append(StringUtils.LINEBREAK);
        for (final DatasetDescription dataset : datasets) {
            table
                    .append("  ")
                    .append(dataset.name).append(LaTeX.TABLE_SEPARATOR)
                    .append(dataset.domain).append(LaTeX.TABLE_SEPARATOR)
                    .append(dataset.commits).append(LaTeX.TABLE_ENDROW);
        }
        table.append("\\end{tabular}").append(StringUtils.LINEBREAK);

        return table.toString();
    }

    /**
     * Returns true iff the given string equals "y" ignoring case and whitespace.
     */
    private static boolean isYes(final String s) {
        return s.trim().equalsIgnoreCase("y");
    }
}

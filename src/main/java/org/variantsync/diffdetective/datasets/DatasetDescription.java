package org.variantsync.diffdetective.datasets;

import org.variantsync.diffdetective.util.LaTeX;
import org.variantsync.diffdetective.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record DatasetDescription(
        String name,
        String repoURL,
        String domain,
        String commits
) {
    public static List<DatasetDescription> fromMarkdown(final Path markdownFile) throws IOException {
        try (Stream<String> lines = Files.lines(markdownFile)) {
            return lines
                .skip(2) // Skip header
                .map(line -> line.split("\\|"))
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

    private static boolean isYes(final String s) {
        return s.trim().equalsIgnoreCase("y");
    }
}

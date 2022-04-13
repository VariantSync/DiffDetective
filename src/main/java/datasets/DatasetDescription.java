package datasets;

import util.FileUtils;
import util.LaTeX;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record DatasetDescription(
        String name,
        String repoURL,
        String domain,
        String commits
) {
    public static List<DatasetDescription> fromMarkdown(final Path markdownFile) throws IOException {
        final String markdown = FileUtils.readUTF8(markdownFile);
        final String[] lines = markdown.split(StringUtils.LINEBREAK_REGEX);

        final List<DatasetDescription> datasets = new ArrayList<>(lines.length - 2);
        // Start at 2 to skip header and separator line of table
        for (int i = 2; i < lines.length; ++i) {
            final String[] cells = lines[i].split("\\|");

            final String hasCode = cells[2];
            final String isGitRepo = cells[3];

            if (isYes(hasCode) && isYes(isGitRepo)) {
                datasets.add(new DatasetDescription(
                        cells[0].trim(), // name
                        cells[5].trim(), // clone URL,
                        cells[1].trim(), // domain
                        cells[6].trim()  // #commits
                ));
            }
        }

        return datasets;
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

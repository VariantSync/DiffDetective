package mining.dataset;

import util.FileUtils;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record MiningDataset(
        String name,
        String repoURL
) {
    public static List<MiningDataset> fromMarkdown(final Path markdownFile) throws IOException {
        final String markdown = FileUtils.readUTF8(markdownFile);
        final String[] lines = markdown.split(StringUtils.LINEBREAK);

        final List<MiningDataset> datasets = new ArrayList<>(lines.length - 2);
        // Start at 2 to skip header and separator line of table
        for (int i = 2; i < lines.length; ++i) {
            final String[] cells = lines[i].split("\\|");

            final String hasCode = cells[2];
            final String isGitRepo = cells[3];

            if (isYes(hasCode) && isYes(isGitRepo)) {
                datasets.add(new MiningDataset(
                        cells[0].trim(), // name
                        cells[5].trim()  // clone URL
                ));
            }
        }

        return datasets;
    }

    private static boolean isYes(final String s) {
        return s.trim().equalsIgnoreCase("y");
    }
}

package org.variantsync.diffdetective.variation.diff.source;

import java.nio.file.Path;
import java.util.List;

import org.variantsync.diffdetective.diff.git.CommitDiff; // For Javadoc
import org.variantsync.diffdetective.util.Source;

/**
 * Describes that a VariationDiff was created from a patch in a {@link CommitDiff}.
 * @param getFileName Name of the modified file from whose changes the VariationDiff was parsed.
 * @param getCommitHash Hash of the commit in which the edit occurred.
 */
public record CommitDiffVariationDiffSource(Path getFileName, String getCommitHash) implements Source {
    @Override
    public String toString() {
        return getFileName() + "@" + getCommitHash();
    }

    @Override
    public String getSourceExplanation() {
        return "CommitDiff";
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of(getFileName(), getCommitHash());
    }
}

package org.variantsync.diffdetective.variation.diff.serialize.treeformat;

import org.apache.commons.io.FilenameUtils;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphConstants;
import org.variantsync.diffdetective.variation.diff.source.CommitDiffVariationDiffSource;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link VariationDiffLabelFormat} for {@link CommitDiff}s and {@link PatchDiff}s.
 * This format labels trees with the patch they originated from.
 * Produced labels will be of the form <code>filename$$$commitHash</code>.
 */
public class CommitDiffVariationDiffLabelFormat implements VariationDiffLabelFormat {
    @Override
    public VariationDiffSource fromLabel(final String label) {
        String[] commit = label.split(LineGraphConstants.TREE_NAME_SEPARATOR_REGEX);
        try {
            Path filePath = Paths.get(commit[0]);
            String commitHash = commit[1];
            return new CommitDiffVariationDiffSource(filePath, commitHash);
        } catch (InvalidPathException e) { 
            throw new RuntimeException("Syntax error. The path cannot be read: " + commit[0]);
        }
    }

    @Override
    public String toLabel(final VariationDiffSource variationDiffSource) {
        if (variationDiffSource instanceof CommitDiffVariationDiffSource commitDiffVariationDiffSource) {
            // write for instances of CommitDiffVariationDiffSources
            return FilenameUtils.separatorsToUnix(commitDiffVariationDiffSource.getFileName().toString()) + LineGraphConstants.TREE_NAME_SEPARATOR + commitDiffVariationDiffSource.getCommitHash();
        } else if (variationDiffSource instanceof PatchDiff patchDiff) {
            // write for instances of PatchDiffs
            return FilenameUtils.separatorsToUnix(patchDiff.getFileName(Time.AFTER)) + LineGraphConstants.TREE_NAME_SEPARATOR + patchDiff.getCommitDiff().getCommitHash();
        } else {
            throw new UnsupportedOperationException("There is no implementation for this VariationDiffSource type: " + variationDiffSource);
        }
    }
}

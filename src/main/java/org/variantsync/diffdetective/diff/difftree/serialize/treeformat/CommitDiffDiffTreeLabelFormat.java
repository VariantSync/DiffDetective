package org.variantsync.diffdetective.diff.difftree.serialize.treeformat;

import org.apache.commons.io.FilenameUtils;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.CommitDiffDiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link DiffTreeLabelFormat} for {@link CommitDiff}s and {@link PatchDiff}s.
 * This format labels trees with the patch they originated from.
 * Produced labels will be of the form <code>filename$$$commitHash</code>.
 */
public class CommitDiffDiffTreeLabelFormat implements DiffTreeLabelFormat {
    @Override
    public DiffTreeSource fromLabel(final String label) {
        String[] commit = label.split(LineGraphConstants.TREE_NAME_SEPARATOR_REGEX);
        try {
            Path filePath = Paths.get(commit[0]);
            String commitHash = commit[1];
            return new CommitDiffDiffTreeSource(filePath, commitHash);
        } catch (InvalidPathException e) { 
            throw new RuntimeException("Syntax error. The path cannot be read: " + commit[0]);
        }
    }

    @Override
    public String toLabel(final DiffTreeSource diffTreeSource) {
        if (diffTreeSource instanceof CommitDiffDiffTreeSource commitDiffDiffTreeSource) {
            // write for instances of CommitDiffDiffTreeSources
            return FilenameUtils.separatorsToUnix(commitDiffDiffTreeSource.getFileName().toString()) + LineGraphConstants.TREE_NAME_SEPARATOR + commitDiffDiffTreeSource.getCommitHash();
        } else if (diffTreeSource instanceof PatchDiff patchDiff) {
            // write for instances of PatchDiffs
            return FilenameUtils.separatorsToUnix(patchDiff.getFileName()) + LineGraphConstants.TREE_NAME_SEPARATOR + patchDiff.getCommitDiff().getCommitHash();
        } else {
            throw new UnsupportedOperationException("There is no implementation for this DiffTreeSource type: " + diffTreeSource);
        }
    }
}

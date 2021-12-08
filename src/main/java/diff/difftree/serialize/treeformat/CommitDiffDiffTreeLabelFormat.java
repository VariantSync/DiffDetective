package diff.difftree.serialize.treeformat;

import diff.CommitDiff;
import diff.PatchDiff;
import diff.difftree.CommitDiffDiffTreeSource;
import diff.difftree.DiffTreeSource;
import diff.difftree.LineGraphConstants;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link DiffTreeLabelFormat} for {@link CommitDiff}.
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
            return commitDiffDiffTreeSource.getFileName() + LineGraphConstants.TREE_NAME_SEPARATOR + commitDiffDiffTreeSource.getCommitHash();
		} else if (diffTreeSource instanceof PatchDiff patchDiff) {
			// write for instances of PatchDiffs
            return patchDiff.getFileName() + LineGraphConstants.TREE_NAME_SEPARATOR + patchDiff.hashCode();
		} else {
			throw new UnsupportedOperationException("There is no implementation for this DiffTreeSource type: " + diffTreeSource);
		}
	}

}

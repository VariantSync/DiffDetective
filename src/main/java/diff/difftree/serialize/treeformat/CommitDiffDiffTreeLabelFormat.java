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
	public DiffTreeSource readTreeHeaderFromLineGraph(final String lineGraphLine) {
		String[] commit = lineGraphLine.split(LineGraphConstants.TREE_NAME_SEPARATOR_REGEX);
		try {
			Path filePath = Paths.get(commit[0]);
			String commitHash = commit[1];
			return new CommitDiffDiffTreeSource(filePath, commitHash);
		} catch (InvalidPathException e) { 
			throw new RuntimeException("Syntax error. The path cannot be read: " + commit[0]);
		}
	}

	@Override
	public String writeTreeHeaderToLineGraph(final DiffTreeSource diffTreeSource) {
		if (diffTreeSource instanceof CommitDiffDiffTreeSource) {
			// write for instances of CommitDiffDiffTreeSources
			CommitDiffDiffTreeSource commitDiffDiffTreeSource = (CommitDiffDiffTreeSource) diffTreeSource;
			return commitDiffDiffTreeSource.getFileName() + LineGraphConstants.TREE_NAME_SEPARATOR + commitDiffDiffTreeSource.getCommitHash();
		} else if (diffTreeSource instanceof PatchDiff) {
			// write for instances of PatchDiffs
			PatchDiff patchDiff = (PatchDiff) diffTreeSource;
			return patchDiff.getFileName() + LineGraphConstants.TREE_NAME_SEPARATOR + patchDiff.hashCode();
		} else {
			throw new UnsupportedOperationException("There is no implementation for this DiffTreeSource type: " + diffTreeSource);
		}
	}

}

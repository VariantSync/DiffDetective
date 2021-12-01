package diff.difftree.serialize.treeformat;

import diff.CommitDiff;
import diff.PatchDiff;
import diff.difftree.CommitDiffDiffTreeSource;
import diff.difftree.DiffTreeSource;
import diff.difftree.LineGraphConstants;

/**
 * A {@link DiffTreeLabelFormat} for {@link CommitDiff}.
 */
public class CommitDiffDiffTreeLabelFormat implements DiffTreeLabelFormat {

	@Override
	public DiffTreeSource readTreeHeaderFromLineGraph(String lineGraphLine) {
		lineGraphLine = lineGraphLine.substring(LineGraphConstants.LG_TREE_HEADER.length(), lineGraphLine.length() - 1);
		String[] commit = lineGraphLine.split(LineGraphConstants.TREE_NAME_SEPARATOR);
		String filePath = commit[0];
		String commitHash = commit[1];
		return new CommitDiffDiffTreeSource(filePath, commitHash);
	}

	@Override
	public String writeTreeHeaderToLineGraph(DiffTreeSource diffTreeSource) {
		if (diffTreeSource instanceof CommitDiffDiffTreeSource) {
			// write for instances of CommitDiffDiffTreeSources
			CommitDiffDiffTreeSource commitDiffDiffTreeSource = (CommitDiffDiffTreeSource) diffTreeSource;
			return LineGraphConstants.LG_TREE_HEADER + commitDiffDiffTreeSource.getFileName() + LineGraphConstants.TREE_NAME_SEPARATOR + commitDiffDiffTreeSource.getCommitHash();
		} else if (diffTreeSource instanceof PatchDiff) {
			// write for instances of PatchDiffs
			PatchDiff patchDiff = (PatchDiff) diffTreeSource;
			return LineGraphConstants.LG_TREE_HEADER + patchDiff.getFileName() + LineGraphConstants.TREE_NAME_SEPARATOR + patchDiff.hashCode();
		} else {
			throw new UnsupportedOperationException("There is no implementation for this DiffTreeSource type: " + diffTreeSource);
		}
	}

}

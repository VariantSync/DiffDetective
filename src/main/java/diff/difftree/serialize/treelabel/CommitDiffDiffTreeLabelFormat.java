package diff.difftree.serialize.treelabel;

import diff.difftree.CommitDiffDiffTreeSource;
import diff.difftree.DiffTreeSource;
import diff.difftree.LineGraphImport;
import diff.serialize.LineGraphExport;

public class CommitDiffDiffTreeLabelFormat implements DiffTreeLabelFormat {

	@Override
	public DiffTreeSource readTreeHeaderFromLineGraph(String lineGraphLine) {
		lineGraphLine = lineGraphLine.substring(LineGraphImport.LG_TREE_HEADER.length(), lineGraphLine.length() - 1);
		String[] commit = lineGraphLine.split(LineGraphExport.TREE_NAME_SEPARATOR);
		String filePath = commit[0];
		String commitHash = commit[1];
		return new CommitDiffDiffTreeSource(filePath, commitHash);
	}

	@Override
	public String writeTreeHeaderToLineGraph(DiffTreeSource diffTreeSource) {
		if (diffTreeSource instanceof CommitDiffDiffTreeSource) {
			CommitDiffDiffTreeSource commitDiffTreeSource = (CommitDiffDiffTreeSource) diffTreeSource;
			return LineGraphImport.LG_TREE_HEADER + commitDiffTreeSource.getFileName() + LineGraphExport.TREE_NAME_SEPARATOR + commitDiffTreeSource.getCommitHash();
		} else {
			throw new RuntimeException("Invalid Type: " + diffTreeSource);
		}
	}

}

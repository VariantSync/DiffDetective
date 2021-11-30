package diff.difftree;

public class CommitDiffDiffTreeSource implements DiffTreeSource {

	private final String fileName;
	private final String commitHash;
	
	public CommitDiffDiffTreeSource(String fileName, String commitHash) {
		this.fileName = fileName;
		this.commitHash = commitHash;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getCommitHash() {
		return commitHash;
	}
	
}

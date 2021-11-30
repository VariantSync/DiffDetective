package diff.difftree;


public class CommitDiffTreeSource implements DiffTreeSource {

	private final String fileName;
	private final String commitHash;
	
	public CommitDiffTreeSource(String fileName, String commitHash) {
		this.fileName = fileName;
		this.commitHash = commitHash;
	}
	
}

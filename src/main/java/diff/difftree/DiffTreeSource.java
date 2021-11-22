package diff.difftree;

public class DiffTreeSource implements IDiffTreeSource {

	private final String fileName;
	private final String commitHash;
	
	public DiffTreeSource(String fileName, String commitHash) {
		this.fileName = fileName;
		this.commitHash = commitHash;
	}
	
}

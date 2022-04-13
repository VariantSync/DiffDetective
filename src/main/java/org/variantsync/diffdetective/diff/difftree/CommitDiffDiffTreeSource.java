package org.variantsync.diffdetective.diff.difftree;

import java.nio.file.Path;

public class CommitDiffDiffTreeSource implements DiffTreeSource {

	private final Path fileName;
	private final String commitHash;
	
	public CommitDiffDiffTreeSource(final Path fileName, final String commitHash) {
		this.fileName = fileName;
		this.commitHash = commitHash;
	}
	
	public Path getFileName() {
		return fileName;
	}
	
	public String getCommitHash() {
		return commitHash;
	}

    @Override
    public String toString() {
        return fileName + "@" + commitHash;
    }
}

package org.variantsync.diffdetective.diff.difftree;

import java.nio.file.Path;

/**
 * Describes that a DiffTree was created from a patch in a {@link org.variantsync.diffdetective.diff.CommitDiff}.
 */
public class CommitDiffDiffTreeSource implements DiffTreeSource {
	private final Path fileName;
	private final String commitHash;

	/**
	 * Create a source that refers to changes to the given file in the given commit.
	 * @param fileName Name of the modified file from whose changes the DiffTree was parsed.
	 * @param commitHash Hash of the commit in which the edit occurred.
	 */
	public CommitDiffDiffTreeSource(final Path fileName, final String commitHash) {
		this.fileName = fileName;
		this.commitHash = commitHash;
	}

	/**
	 * Returns the name of the modified file from whose changes the DiffTree was parsed.
	 */
	public Path getFileName() {
		return fileName;
	}

	/**
	 * Returns the hash of the commit in which the edit occurred.
	 */
	public String getCommitHash() {
		return commitHash;
	}

    @Override
    public String toString() {
        return fileName + "@" + commitHash;
    }
}

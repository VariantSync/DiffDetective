package org.variantsync.diffdetective.diff;

/**
 * A unique reference to a diff of a file (patch) within an unspecified repository.
 *
 * @param getFileName the name of the file which was modified
 * @param getCommitHash the id of the state after the edit
 * @param getParentCommitHash the id of the state before the edit
 *
 * @author Paul Bittner, Benjamin Moosherr
 */
public record PatchReference(
    String getFileName,
    String getCommitHash,
    String getParentCommitHash
) {
}

package org.variantsync.diffdetective.diff.difftree.source;

import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

import java.nio.file.Path;

/**
 * A source for DiffTree's that were created from patch files on disk.
 * @param path Path to the patch file that was parsed to a DiffTree.
 */
public record PatchFile(Path path) implements DiffTreeSource {
}

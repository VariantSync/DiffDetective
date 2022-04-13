package org.variantsync.diffdetective.diff.difftree.source;

import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

import java.nio.file.Path;

public record PatchFile(Path path) implements DiffTreeSource {

}

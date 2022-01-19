package diff.difftree.source;

import diff.difftree.DiffTreeSource;

import java.nio.file.Path;

public record PatchFile(Path path) implements DiffTreeSource {

}

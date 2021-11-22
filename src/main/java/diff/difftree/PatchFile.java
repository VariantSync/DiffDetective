package diff.difftree;

import java.nio.file.Path;

public record PatchFile(Path path) implements IDiffTreeSource {

}

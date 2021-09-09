import diff.difftree.DiffTree;
import diff.difftree.parse.DiffTreeParser;
import diff.difftree.traverse.DiffTreeTraversal;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;

public class TestLineNumbers {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs");

    @Test
    public void printLineNumbers() throws IOException {
        final String fullDiff = IO.readAsString(resDir.resolve("lineno1.txt"));
        final DiffTree diffTree = DiffTreeParser.createDiffTree(fullDiff, false, false);

        assert diffTree != null;
        diffTree.forAll(node ->
            System.out.println(node.diffType.name
                    + " \"" + node.getText().trim()
                    + "\" from " + node.getFromLine()
                    + " to " + node.getToLine())
        );
    }
}

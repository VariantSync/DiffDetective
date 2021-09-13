import diff.difftree.DiffTree;
import diff.difftree.parse.DiffTreeParser;
import diff.difftree.traverse.DiffTreeTraversal;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class TestLineNumbers {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/linenumbers");

    private static void printLineNumbers(final Path p) throws IOException {
        final String fullDiff = IO.readAsString(p);
        final DiffTree diffTree = DiffTreeParser.createDiffTree(fullDiff, false, false);

        assert diffTree != null;
        diffTree.forAll(node ->
                System.out.println(node.diffType.name
                        + " \"" + node.getText().trim()
                        + "\" of type " + node.codeType
                        + " from " + node.getFromLine()
                        + " to " + node.getToLine())
        );
    }

    @Test
    public void printLineNumbers() throws IOException {
        final Collection<String> testCases = List.of(
//                "lineno1.txt",
//                "deleteMLM.txt",
                "elifchain.txt"
        );

        for (final String s : testCases) {
            System.out.println("Diff of " + s);
            printLineNumbers(resDir.resolve(s));
            System.out.println();
        }
    }
}

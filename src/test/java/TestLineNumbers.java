import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.functjonal.Pair;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TestLineNumbers {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/linenumbers");
    private record TestCase(String filename, Map<Integer, Pair<DiffLineNumber, DiffLineNumber>> expectedLineNumbers) { }

    public static List<TestCase> testCases() {
        // Testcases rely on stability of IDs

        final var elifchain_map = new HashMap<Integer, Pair<DiffLineNumber, DiffLineNumber>>();
        elifchain_map.put(16, new Pair<>(DiffLineNumber.Invalid(), DiffLineNumber.Invalid()));
        elifchain_map.put(147, new Pair<>(new DiffLineNumber(1, 1, 1), new DiffLineNumber(2, 2, 2)));
        elifchain_map.put(208, new Pair<>(new DiffLineNumber(2, 2, 2), new DiffLineNumber(4, 4, 4)));
        elifchain_map.put(275, new Pair<>(new DiffLineNumber(3, 3, 3), new DiffLineNumber(4, 4, 4)));
        elifchain_map.put(338, new Pair<>(new DiffLineNumber(4, 4, 4), new DiffLineNumber(8, 6, 6)));
        elifchain_map.put(403, new Pair<>(new DiffLineNumber(5, 5, 5), new DiffLineNumber(6, 6, 6)));
        elifchain_map.put(450, new Pair<>(new DiffLineNumber(6, -1, 6), new DiffLineNumber(10, -1, 9)));
        elifchain_map.put(515, new Pair<>(new DiffLineNumber(7, -1, 7), new DiffLineNumber(8, -1, 8)));
        elifchain_map.put(659, new Pair<>(new DiffLineNumber(9, 7, 8), new DiffLineNumber(10, 8, 9)));
        elifchain_map.put(586, new Pair<>(new DiffLineNumber(8, 6, -1), new DiffLineNumber(10, 8, -1)));
        TestCase elifchain = new TestCase("elifchain.txt", elifchain_map);

        final var lineno1_map = new HashMap<Integer, Pair<DiffLineNumber, DiffLineNumber>>();
        lineno1_map.put(16, new Pair<>(DiffLineNumber.Invalid(), DiffLineNumber.Invalid()));
        lineno1_map.put(147, new Pair<>(new DiffLineNumber(1, 1, 1), new DiffLineNumber(2, 2, 2)));
        lineno1_map.put(208, new Pair<>(new DiffLineNumber(2, 2, 2), new DiffLineNumber(4, 4, 4)));
        lineno1_map.put(275, new Pair<>(new DiffLineNumber(3, 3, 3), new DiffLineNumber(4, 4, 4)));
        lineno1_map.put(403, new Pair<>(new DiffLineNumber(5, 5, 5), new DiffLineNumber(6, 6, 6)));
        lineno1_map.put(451, new Pair<>(new DiffLineNumber(6, -1, 6), new DiffLineNumber(7, -1, 7)));
        lineno1_map.put(523, new Pair<>(new DiffLineNumber(7, 6, -1), new DiffLineNumber(8, 7, -1)));
        lineno1_map.put(595, new Pair<>(new DiffLineNumber(8, 7, 7), new DiffLineNumber(9, 8, 8)));
        lineno1_map.put(640, new Pair<>(new DiffLineNumber(9, -1, 8), new DiffLineNumber(11, -1, 10)));
        lineno1_map.put(723, new Pair<>(new DiffLineNumber(10, 8, 9), new DiffLineNumber(11, 9, 10)));
        lineno1_map.put(851, new Pair<>(new DiffLineNumber(12, 9, 11), new DiffLineNumber(13, 10, 12)));
        lineno1_map.put(904, new Pair<>(new DiffLineNumber(13, 10, -1), new DiffLineNumber(16, 13, -1)));
        lineno1_map.put(1035, new Pair<>(new DiffLineNumber(15, 12, -1), new DiffLineNumber(16, 13, -1)));
        TestCase lineno1 = new TestCase("lineno1.txt", lineno1_map);

        final var deleteMLM_map = new HashMap<Integer, Pair<DiffLineNumber, DiffLineNumber>>();
        deleteMLM_map.put(16, new Pair<>(DiffLineNumber.Invalid(), DiffLineNumber.Invalid()));
        deleteMLM_map.put(136, new Pair<>(new DiffLineNumber(1, 1, -1), new DiffLineNumber(4, 4, -1)));
        deleteMLM_map.put(267, new Pair<>(new DiffLineNumber(3, 3, -1), new DiffLineNumber(4, 4, -1)));
        TestCase deleteMLM = new TestCase("deleteMLM.txt", deleteMLM_map);

        return List.of(elifchain, lineno1, deleteMLM);
    }

    private static DiffTree loadFullDiff(final Path p) throws IOException, DiffParseException {
        return DiffTree.fromFile(p, false, false);
    }

    private static void printLineNumbers(final DiffTree diffTree) {
        diffTree.forAll(node ->
                System.out.println(node.diffType.symbol
                    + " " + node.nodeType
                    + " \"" + node.getLabel().trim()
                    + " with ID " + node.getID()
                    + "\" old: " + node.getLinesBeforeEdit()
                    + ", diff: " + node.getLinesInDiff()
                    + ", new: " + node.getLinesAfterEdit())
        );
        System.out.println();
    }

    private static String generateTestCaseCode(final Path p) throws IOException, DiffParseException {
        final DiffTree diffTree = loadFullDiff(p);
        final Function<DiffLineNumber, String> toConstructorCall = l ->
                "new DiffLineNumber(" + l.inDiff() + ", " + l.beforeEdit() + ", " + l.afterEdit() + ")";
        String testName = p.getFileName().toString();
        testName = testName.substring(0, testName.lastIndexOf("."));
        String mapName = testName + "_map";

        System.out.println("final var " + mapName + " = new HashMap<Integer, Pair<DiffLineNumber, DiffLineNumber>>();");
        diffTree.forAll(node ->
                System.out.println(mapName + ".put(" + node.getID()
                        + ", new Pair<>("
                        + toConstructorCall.apply(node.getFromLine())
                        + ", "
                        + toConstructorCall.apply(node.getToLine())
                        + "));")
        );
        System.out.println("TestCase " + testName + " = new TestCase(\"" + p.getFileName() + "\", " + mapName + ");");
        System.out.println();
        return testName;
    }

//    @Test
    public void generateTestCode() throws IOException, DiffParseException {
        final StringBuilder listof = new StringBuilder("List.of(");
        boolean first = true;
        for (final TestCase s : testCases()) {
            if (first) {
                first = false;
            } else {
                listof.append(", ");
            }
            listof.append(generateTestCaseCode(resDir.resolve(s.filename())));
        }
        listof.append(");");
        System.out.println("testCases = " + listof);
    }

//    @ParameterizedTest
    @MethodSource("testCases")
    public void printLineNumbers(TestCase testCase) throws IOException, DiffParseException {
        System.out.println("Diff of " + testCase.filename());
        printLineNumbers(loadFullDiff(resDir.resolve(testCase.filename())));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testLineNumbers(TestCase testCase) throws IOException, DiffParseException {
        final DiffTree t = loadFullDiff(resDir.resolve(testCase.filename()));
        t.forAll(node -> {
            var fromTo = testCase.expectedLineNumbers.get(node.getID());
            final DiffLineNumber from = fromTo.first();
            final DiffLineNumber to = fromTo.second();
            assertEquals(from, node.getFromLine());
            assertEquals(to, node.getToLine());
        });
    }
}

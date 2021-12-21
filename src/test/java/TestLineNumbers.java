import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.DiffLineNumber;
import diff.difftree.DiffTree;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TestLineNumbers {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/linenumbers");
    private record TestCase(String filename, Map<Integer, Pair<DiffLineNumber, DiffLineNumber>> expectedLineNumbers) { }
    private List<TestCase> testCases;

    @Before
    public void initTestCases() {
        // Testcases rely on stability of IDs
//        testCases = List.of(
//                new TestCase("elifchain.txt", Map.ofEntries())
//                , new TestCase("lineno1.txt", Map.of())
//                , new TestCase("deleteMLM.txt", Map.ofEntries())
//        );

        final var elifchain_map = new HashMap<Integer, Pair<DiffLineNumber, DiffLineNumber>>();
        elifchain_map.put(131589, new Pair<>(new DiffLineNumber(1, 1, 1), new DiffLineNumber(11, 9, 10)));
        elifchain_map.put(131588, new Pair<>(new DiffLineNumber(1, 1, 1), new DiffLineNumber(2, 2, 2)));
        elifchain_map.put(197120, new Pair<>(new DiffLineNumber(2, 2, 2), new DiffLineNumber(4, 4, 4)));
        elifchain_map.put(262660, new Pair<>(new DiffLineNumber(3, 3, 3), new DiffLineNumber(4, 4, 4)));
        elifchain_map.put(328195, new Pair<>(new DiffLineNumber(4, 4, 4), new DiffLineNumber(8, 6, 6)));
        elifchain_map.put(393732, new Pair<>(new DiffLineNumber(5, 5, 5), new DiffLineNumber(6, 6, 6)));
        elifchain_map.put(458755, new Pair<>(new DiffLineNumber(6, -1, 6), new DiffLineNumber(10, -1, 9)));
        elifchain_map.put(524292, new Pair<>(new DiffLineNumber(7, -1, 7), new DiffLineNumber(8, -1, 8)));
        elifchain_map.put(655876, new Pair<>(new DiffLineNumber(9, 7, 8), new DiffLineNumber(10, 8, 9)));
        elifchain_map.put(590083, new Pair<>(new DiffLineNumber(8, 6, -1), new DiffLineNumber(10, 8, -1)));
        TestCase elifchain = new TestCase("elifchain.txt", elifchain_map);

        final var lineno1_map = new HashMap<Integer, Pair<DiffLineNumber, DiffLineNumber>>();
        lineno1_map.put(131589, new Pair<>(new DiffLineNumber(1, 1, 1), new DiffLineNumber(17, 14, 12)));
        lineno1_map.put(131588, new Pair<>(new DiffLineNumber(1, 1, 1), new DiffLineNumber(2, 2, 2)));
        lineno1_map.put(197120, new Pair<>(new DiffLineNumber(2, 2, 2), new DiffLineNumber(4, 4, 4)));
        lineno1_map.put(262660, new Pair<>(new DiffLineNumber(3, 3, 3), new DiffLineNumber(4, 4, 4)));
        lineno1_map.put(393732, new Pair<>(new DiffLineNumber(5, 5, 5), new DiffLineNumber(6, 6, 6)));
        lineno1_map.put(458756, new Pair<>(new DiffLineNumber(6, -1, 6), new DiffLineNumber(7, -1, 7)));
        lineno1_map.put(524548, new Pair<>(new DiffLineNumber(7, 6, -1), new DiffLineNumber(8, 7, -1)));
        lineno1_map.put(590340, new Pair<>(new DiffLineNumber(8, 7, 7), new DiffLineNumber(9, 8, 8)));
        lineno1_map.put(655360, new Pair<>(new DiffLineNumber(9, -1, 8), new DiffLineNumber(11, -1, 10)));
        lineno1_map.put(721412, new Pair<>(new DiffLineNumber(10, 8, 9), new DiffLineNumber(11, 9, 10)));
        lineno1_map.put(852484, new Pair<>(new DiffLineNumber(12, 9, 11), new DiffLineNumber(13, 10, 12)));
        lineno1_map.put(917760, new Pair<>(new DiffLineNumber(13, 10, -1), new DiffLineNumber(16, 13, -1)));
        lineno1_map.put(1048836, new Pair<>(new DiffLineNumber(15, 12, -1), new DiffLineNumber(16, 13, -1)));
        TestCase lineno1 = new TestCase("lineno1.txt", lineno1_map);

        final var deleteMLM_map = new HashMap<Integer, Pair<DiffLineNumber, DiffLineNumber>>();
        deleteMLM_map.put(131589, new Pair<>(new DiffLineNumber(1, 1, 1), new DiffLineNumber(5, 5, 1)));
        deleteMLM_map.put(131328, new Pair<>(new DiffLineNumber(1, 1, -1), new DiffLineNumber(4, 4, -1)));
        deleteMLM_map.put(262404, new Pair<>(new DiffLineNumber(3, 3, -1), new DiffLineNumber(4, 4, -1)));
        TestCase deleteMLM = new TestCase("deleteMLM.txt", deleteMLM_map);

        testCases = List.of(elifchain, lineno1, deleteMLM);
    }

    private static DiffTree loadFullDiff(final Path p) throws IOException {
        return DiffTree.fromFile(p, false, false);
    }

    private static void printLineNumbers(final DiffTree diffTree) {
        diffTree.forAll(node ->
                System.out.println(node.diffType.symbol
                    + " " + node.codeType
                    + " \"" + node.getLabel().trim()
                    + " with ID " + node.getID()
                    + "\" old: " + node.getLinesBeforeEdit()
                    + ", diff: " + node.getLinesInDiff()
                    + ", new: " + node.getLinesAfterEdit())
        );
        System.out.println();
    }

    private static String generateTestCaseCode(final Path p) throws IOException {
        final DiffTree diffTree = loadFullDiff(p);
        final Function<DiffLineNumber, String> toConstructorCall = l ->
                "new DiffLineNumber(" + l.inDiff + ", " + l.beforeEdit + ", " + l.afterEdit + ")";
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
    public void generateTestCode() throws IOException {
        final StringBuilder listof = new StringBuilder("List.of(");
        boolean first = true;
        for (final TestCase s : testCases) {
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

//    @Test
    public void printLineNumbers() throws IOException {
        for (final TestCase s : testCases) {
            System.out.println("Diff of " + s.filename());
            printLineNumbers(loadFullDiff(resDir.resolve(s.filename())));
        }
    }

    @Test
    public void testLineNumbers() throws IOException {
        for (final TestCase s : testCases) {
            final DiffTree t = loadFullDiff(resDir.resolve(s.filename()));
            t.forAll(node -> {
                var fromTo = s.expectedLineNumbers.get(node.getID());
                final DiffLineNumber from = fromTo.getKey();
                final DiffLineNumber to = fromTo.getValue();
                Assert.assertEquals(from, node.getFromLine());
                Assert.assertEquals(to, node.getToLine());
            });
        }
    }
}

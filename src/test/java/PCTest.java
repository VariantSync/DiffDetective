import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.result.DiffParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class PCTest {
    private static final Literal A = new Literal("A");
    private static final Literal B = new Literal("B");
    private static final Literal C = new Literal("C");
    private static final Literal D = new Literal("D");
    private static final Literal E = new Literal("E");
    record ExpectedPC(Node before, Node after) {}
    record TestCase(Path file, Map<String, ExpectedPC> expectedResult) {
        @Override
        public String toString() {
            return file.toString();
        }
    }

    private final static Path testDir = Constants.RESOURCE_DIR.resolve("pctest");
    private final static TestCase a = new TestCase(
            Path.of("a.diff"),
            Map.of(
                    "1", new ExpectedPC(A, new And(A, B)),
                    "2", new ExpectedPC(A, new And(A, C, negate(B))),
                    "3", new ExpectedPC(new And(A, D, E), new And(A, D)),
                    "4", new ExpectedPC(A, A)
            ));
    private final static TestCase elif = new TestCase(
            Path.of("elif.diff"),
            Map.of(
                    "1", new ExpectedPC(A, A),
                    "2", new ExpectedPC(new And(negate(A), B), new And(negate(A), B)),
                    "3", new ExpectedPC(new And(negate(A), negate(B), C), new And(negate(A), B)),
                    "4", new ExpectedPC(new And(negate(A), negate(B), C), new And(negate(A), negate(B), D)),
                    "5", new ExpectedPC(new And(negate(A), negate(B), negate(C)), new And(negate(A), negate(B), negate(D)))
            ));
    private final static TestCase elze = new TestCase(
            Path.of("else.diff"),
            Map.of(
                    "1", new ExpectedPC(A, new And(A, B)),
                    "2", new ExpectedPC(new And(negate(A), C), new And(A, negate(B), C)),
                    "3", new ExpectedPC(new And(negate(A), C), new And(A, negate(B), negate(C))),
                    "4", new ExpectedPC(new And(negate(A), negate(C)), negate(A))
            ));

    private static String errorAt(final String node, String time, Node is, Node should) {
        return time + " PC of node \"" + node + "\" is \"" + is + "\" but expected \"" + should + "\"!";
    }

    public static List<TestCase> testCases() {
        return List.of(a, elif, elze);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void test(final TestCase testCase) throws IOException, DiffParseException {
        final Path path = testDir.resolve(testCase.file);
        final DiffTree t = DiffTree.fromFile(path, false, true);
        t.forAll(node -> {
           if (node.isArtifact()) {
               final String text = node.getLabel().trim();
               final ExpectedPC expectedPC = testCase.expectedResult.getOrDefault(text, null);
               if (expectedPC != null) {
                   Node pc = node.getBeforePresenceCondition();
                   assertTrue(
                           SAT.equivalent(pc, expectedPC.before),
                           errorAt(text, "before", pc, expectedPC.before));
                   pc = node.getAfterPresenceCondition();
                   assertTrue(
                           SAT.equivalent(pc, expectedPC.after),
                           errorAt(text, "after", pc, expectedPC.after));
               } else {
                   Logger.warn("No expected PC specified for node '{}'!", text);
               }
           }
        });
    }
}

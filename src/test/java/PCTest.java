import org.junit.Assert;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class PCTest {
    private static final Literal A = new Literal("A");
    private static final Literal B = new Literal("B");
    private static final Literal C = new Literal("C");
    private static final Literal D = new Literal("D");
    private static final Literal E = new Literal("E");
    record ExpectedPC(Node before, Node after) {}
    record TestCase(Path file, Map<String, ExpectedPC> expectedResult) {}

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

    private void test(final TestCase testCase) throws IOException {
        final Path path = testDir.resolve(testCase.file);
        final DiffTree t = DiffTree.fromFile(path, false, true).unwrap().getSuccess();
        t.forAll(node -> {
           if (node.isCode()) {
               final String text = node.getLabel().trim();
               final ExpectedPC expectedPC = testCase.expectedResult.getOrDefault(text, null);
               if (expectedPC != null) {
                   Node pc = node.getBeforePresenceCondition();
                   Assert.assertTrue(
                           errorAt(text, "before", pc, expectedPC.before),
                           SAT.equivalent(pc, expectedPC.before));
                   pc = node.getAfterPresenceCondition();
                   Assert.assertTrue(
                           errorAt(text, "after", pc, expectedPC.after),
                           SAT.equivalent(pc, expectedPC.after));
               } else {
                   Logger.warn("No expected PC specified for node \"" + text + "\"!");
               }
           }
        });
    }

    @Test
    public void testA() throws IOException {
        test(a);
    }

    @Test
    public void testElif() throws IOException {
        test(elif);
    }

    @Test
    public void testElse() throws IOException {
        test(elze);
    }
}

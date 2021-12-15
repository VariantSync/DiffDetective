import analysis.SAT;
import diff.difftree.DiffTree;
import org.junit.Assert;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class PCTest {
    enum Feature {
        A, B, C, D, E;

        Node and(Feature other) {
            return and(other.toFormula());
        }

        Node and(Node other) {
            return new And(toFormula(), other);
        }

        Node negate() {
            return new Not(toFormula());
        }

        Node toFormula() {
            return new Literal(name());
        }
    }
    record ExpectedPC(Node before, Node after) {
        ExpectedPC(Feature before, Node after) {
            this(before.toFormula(), after);
        }
        ExpectedPC(Feature before, Feature after) {
            this(before.toFormula(), after.toFormula());
        }
    }
    record TestCase(Path file, Map<String, ExpectedPC> expectedResult) {}

    private final static Path testDir = Constants.RESOURCE_DIR.resolve("pctest");
    private final static TestCase a = new TestCase(Path.of("a.diff"),
            Map.of(
                    "1", new ExpectedPC(Feature.A, Feature.A.and(Feature.B)),
                    "2", new ExpectedPC(Feature.A, Feature.A.and(Feature.C.and(Feature.B.negate())))
            ));

    private static String errorAt(final String node, String time, Node is, Node should) {
        return time + " PC of node \"" + node + "\" is \"" + is + "\" but expected \"" + should + "\"!";
    }

    private void test(final TestCase testCase) throws IOException {
        final Path path = testDir.resolve(testCase.file);
        final DiffTree t = DiffTree.fromFile(path, false, true);
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
    public void testAll() throws IOException {
        test(a);
    }
}

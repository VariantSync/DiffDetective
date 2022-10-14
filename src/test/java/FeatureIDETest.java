import de.ovgu.featureide.fm.core.editing.NodeCreator;
import org.junit.jupiter.api.Test;
import org.prop4j.*;
import org.variantsync.diffdetective.analysis.logic.SAT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

public class FeatureIDETest {
    private static Node createTrue() {
        return new True();
//        return new Literal(NodeCreator.varTrue);
    }

    private static Node createFalse() {
        return new False();
//        return new Literal(NodeCreator.varFalse);
    }

    @Test
    public void trueIsTaut() {
        assertTrue(SAT.isTautology(createTrue()));
    }

    @Test
    public void falseIsContradiction() {
        assertFalse(SAT.isSatisfiable(createFalse()));
    }

    /**
     * Reveals a bug reported in issue 1333 (https://github.com/FeatureIDE/FeatureIDE/issues/1333).
     */
    @Test
    public void trueAndA_Equals_A() {
        final Node tru = createTrue();
        final Node a = new Literal("A");
        final Node trueAndA = new And(tru, a);
        assertTrue(SAT.equivalent(trueAndA, a));
    }

    /**
     * Reveals a bug reported in issue 1333 (https://github.com/FeatureIDE/FeatureIDE/issues/1333).
     */
    @Test
    public void A_Equals_A() {
        final Node a = new Literal("A");
        assertTrue(SAT.equivalent(a, a));
    }

    @Test
    public void falseOrA_Equals_A() {
        final Node no = createFalse();
        final Node a = new Literal("A");
        final Node noOrA = new Or(no, a);
        assertTrue(SAT.equivalent(noOrA, a));
    }

    // The following three tests failed and where reported in Issue 1111 (https://github.com/FeatureIDE/FeatureIDE/issues/1111).
    // They work as expected now.

    @Test
    public void atomString() {
        // assume the following does not crash
        createTrue().toString();
        createFalse().toString();
        new And(createFalse(), createTrue()).toString();
    }

    @Test
    public void atomValuesEqual() {
        assertEquals(createTrue(), new Literal(NodeCreator.varTrue));
        assertEquals(createFalse(), new Literal(NodeCreator.varFalse));
    }

    @Test
    public void noAssignmentOfAtomsNecessary() {
        final Map<Object, Boolean> emptyAssignment = new HashMap<>();
        Node formula = new And(createFalse(), createTrue());
        formula.getValue(emptyAssignment);
    }

//    @Test
//    public void ontest() {
//        final Node tru = createTrue();
//        final Node a = new Literal("A");
//        final Node trueAndA = new And(tru, a);
//        final Node eq = new Equals(trueAndA, a);
//        System.out.println(eq);
//        System.out.println(FixTrueFalse.On(eq));
//    }
}

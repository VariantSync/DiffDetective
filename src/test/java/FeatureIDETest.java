import analysis.SAT;
import de.ovgu.featureide.fm.core.editing.NodeCreator;
import org.junit.Assert;
import org.junit.Test;
import org.prop4j.*;

import java.util.HashMap;
import java.util.Map;

public class FeatureIDETest {
    /**
     * Reveals a bug reported in issue 1333 (https://github.com/FeatureIDE/FeatureIDE/issues/1333).
     */
    @Test
    public void trueAndA_Equals_A() {
        final Node tru = new True();
        final Node a = new Literal("A");
        final Node trueAndA = new And(tru, a);
        Assert.assertTrue(SAT.equivalent(trueAndA, a));
    }

    @Test
    public void falseOrA_Equals_A() {
        final Node no = new False();
        final Node a = new Literal("A");
        final Node noOrA = new Or(no, a);
        Assert.assertTrue(SAT.equivalent(noOrA, a));
    }

    // The following three tests failed and where reported in Issue 1111 (https://github.com/FeatureIDE/FeatureIDE/issues/1111).
    // They work as expected now.

    @Test
    public void atomString() {
        System.out.println(new True().toString());
        System.out.println(new False().toString());
        System.out.println(new And(new False(), new True()).toString());
    }

    @Test
    public void atomValuesEqual() {
        Assert.assertEquals(new True(), new Literal(NodeCreator.varTrue));
        Assert.assertEquals(new False(), new Literal(NodeCreator.varFalse));
    }

    @Test
    public void noAssignmentOfAtomsNecessary() {
        final Map<Object, Boolean> emptyAssignment = new HashMap<>();
        Node formula = new And(new False(), new True());
        System.out.println(formula.getValue(emptyAssignment));
    }
}

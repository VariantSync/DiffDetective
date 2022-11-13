import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.NodeType;

public class StaticAsserts {
    @Test
    void testDiffTypeBitCount() {
        assertTrue(
            DiffType.values().length <= Math.pow(2, DiffType.getRequiredBitCount()),
            "Using `DiffType.getRequiredBitCount()` bits is not enough to store all values of `DiffType`"
        );
    }

    @Test
    void testNodeTypeBitCount() {
        assertTrue(
            NodeType.values().length <= Math.pow(2, NodeType.getRequiredBitCount()),
            "Using `NodeType.getRequiredBitCount()` bits is not enough to store all values of `NodeType`"
        );
    }
}

package util.fide;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;

public class FormulaUtils {
    public static Node negate(final Node node) {
        if (node instanceof Literal l) {
            return negate(l);
        }

        return new Not(node);
    }

    public static Literal negate(final Literal lit) {
        if (FixTrueFalse.isTrueLiteral(lit)) {
            return FixTrueFalse.False;
        }
        if (FixTrueFalse.isFalseLiteral(lit)) {
            return FixTrueFalse.True;
        }
        return new Literal(lit.var, !lit.positive);
    }
}

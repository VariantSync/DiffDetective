package util;

import de.ovgu.featureide.fm.core.editing.NodeCreator;
import org.prop4j.Literal;
import org.prop4j.Node;

import java.util.Arrays;
import java.util.List;

/**
 * Class to fix bugs related to True and False classes of FeatureIDE.
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1111
 *
 * This class contains constants for representing atomic values true and false in formulas
 * as well as a conversion method for parsing certain feature names to true and false, respectively.
 */
public class FixTrueFalse {
    /// Names of variables that we want to interpret as atomic values true or false, respectively.
    public final static List<String> TrueNames = Arrays.asList("true", "1");
    public final static List<String> FalseNames = Arrays.asList("false", "0");

    /*
    Constant literals representing the true and false value
     */
    public final static Literal True = new Literal(NodeCreator.varTrue);
    public final static Literal False = new Literal(NodeCreator.varFalse);

    /**
     * @return True iff the given formula is a true literal.
     * @see FixTrueFalse::isTrueLiteral
     */
    public static boolean isTrue(final Node n) {
        return n instanceof Literal l && isTrueLiteral(l);
    }

    /**
     * @return True iff the given formula is a false literal.
     * @see FixTrueFalse::isFalseLiteral
     */
    public static boolean isFalse(final Node n) {
        return n instanceof Literal l && isFalseLiteral(l);
    }

    /**
     * @return True iff the given name represents the atomic value true w.r.t. the constant TrueNames.
     */
    public static boolean isTrueLiteral(final Literal l) {
        return TrueNames.stream().anyMatch(t -> t.equals(l.var.toString().toLowerCase()));
    }

    /**
     * @return True iff the given name represents the atomic value false w.r.t. the constant FalseNames.
     */
    public static boolean isFalseLiteral(final Literal l) {
        return FalseNames.stream().anyMatch(f -> f.equals(l.var.toString().toLowerCase()));
    }

    /**
     * Replaces all literals in the given `formula` with the literals True and False that
     * represent the respective atomic values w.r.t. FixTrueFalse::isTrueLiteral and FixTrueFalse::isFalseLiteral.
     * This e.g. includes replacing literals representing variables with name "1" or "true" with the respective constants.
     *
     * Mutates the given object `formula` and it should be used after invoking this method.
     * Instead, the returned node should be used.
     * @return A formula with a consistent representation of true and false values.
     */
    public static Node On(final Node formula) {
        if (formula instanceof org.prop4j.True) {
            return True;
        }
        if (formula instanceof org.prop4j.False) {
            return False;
        }
        if (formula instanceof Literal l) {
            if (isTrueLiteral(l)) {
                return l.positive ? True : False;
            }
            if (isFalseLiteral(l)) {
                return l.positive ? False : True;
            }
            return l;
        }

        // else we have an operator (Not, And, Or, ...)
        final Node[] children = formula.getChildren();
        for (int i = 0; i < children.length; ++i) {
            children[i] = On(children[i]);
        }
        return formula;
    }
}
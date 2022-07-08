package org.variantsync.diffdetective.feature;

import org.prop4j.Node;
import org.prop4j.NodeReader;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;

/**
 * A parser that parses propositional formula's from text to {@link Node}s.
 * @author Paul Bittner
 */
@FunctionalInterface
public interface PropositionalFormulaParser {
    /*
     * Parses a formula from a string.
     * @param text A propositional formula written as text.
     * @return The formula if parsing succeeded. Null if parsing failed somehow.
     */
    Node parse(String text);

    /**
     * Default parser that uses the {@link NodeReader} from FeatureIDE
     * and uses its {@link NodeReader#activateJavaSymbols() java symbols} to
     * match operators.
     */
    PropositionalFormulaParser Default = text -> {
        final NodeReader nodeReader = new NodeReader();
        nodeReader.activateJavaSymbols();

        Node node = nodeReader.stringToNode(text);

        // if parsing succeeded
        if (node != null) {
            node = FixTrueFalse.EliminateTrueAndFalseInplace(node);
        }

        return node;
    };
}

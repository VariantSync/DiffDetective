package feature;

import org.prop4j.Node;
import org.prop4j.NodeReader;
import util.fide.FixTrueFalse;

@FunctionalInterface
public interface PropositionalFormulaParser {
    /*
     * Parses a formula from a string.
     * @param text A propositional formula written as text.
     * @return The formula if parsing succeeded. Null if parsing failed somehow.
     */
    Node parse(String text);

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

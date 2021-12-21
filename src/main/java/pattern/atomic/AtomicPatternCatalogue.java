package pattern.atomic;

import diff.difftree.DiffNode;
import diff.difftree.DiffType;

import java.util.List;
import java.util.Map;

public interface AtomicPatternCatalogue {
    /**
     * Gives a list of all atomic patterns in this catalogue.
     * The list should be constant and immutable (i.e., the very same list is returned each time all is invoked).
     * The atomic patterns should be immutable, too.
     * @return a constant list of all atomic patterns in this catalogue.
     */
    List<AtomicPattern> all();

    /**
     * Returns a mapping from diff types to the atomic patterns that may match nodes of the given diff type.
     * The returned map as well as all its values should be immutable and constant.     *
     * @return A classification of atomic patterns by their diff types.
     */
    Map<DiffType, List<AtomicPattern>> byType();

    /**
     * Returns the atomic pattern that matches the given node.
     * Each node matches exactly one pattern.
     * @param node The node of which to find its atomic pattern.
     * @return Returns the atomic pattern that matches the given node.
     */
    default AtomicPattern match(DiffNode node)
    {
        if (!node.isCode()) {
            throw new IllegalArgumentException("Expected a code node but got " + node.codeType + "!");
        }

        final List<AtomicPattern> patternsToCheck = byType().get(node.diffType);

        AtomicPattern match = null;
        for (final AtomicPattern p : patternsToCheck) {
            if (p.matches(node)) {
                if (match != null) {
                    throw new RuntimeException("BUG: Error in atomic pattern definition!\n"
                            + "Node " + node + " matched " + match + " and " + p + "!");
                }
                match = p;
            }
        }

        if (match == null) {
            throw new RuntimeException("BUG: Error in atomic pattern definition!\n"
                    + "Node " + node + " did not match any pattern!");
        }

        return match;
    }
}

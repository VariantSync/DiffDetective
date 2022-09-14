package org.variantsync.diffdetective.pattern.elementary;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;

import java.util.List;
import java.util.Map;

/**
 * Interface for custom catalogs of elementary edit patterns.
 * @author Paul Bittner
 */
public interface ElementaryPatternCatalogue {
    /**
     * Gives a list of all elementary patterns in this catalogue.
     * The list should be constant and immutable (i.e., the very same list is returned each time all is invoked).
     * The elementary patterns should be immutable, too.
     * @return a constant list of all elementary patterns in this catalogue.
     */
    List<ElementaryPattern> all();

    /**
     * Returns a mapping from diff types to the elementary patterns that may match nodes of the given diff type.
     * The returned map as well as all its values should be immutable and constant.     *
     * @return A classification of elementary patterns by their diff types.
     */
    Map<DiffType, List<ElementaryPattern>> byType();

    /**
     * Returns the elementary pattern that matches the given node.
     * Each node matches exactly one pattern.
     * @param node The node of which to find its elementary pattern.
     * @return Returns the elementary pattern that matches the given node.
     */
    default ElementaryPattern match(DiffNode node)
    {
        if (!node.isArtifact()) {
            throw new IllegalArgumentException("Expected an artifact node but got " + node.nodeType + "!");
        }

        final List<ElementaryPattern> patternsToCheck = byType().get(node.diffType);

        ElementaryPattern match = null;
        for (final ElementaryPattern p : patternsToCheck) {
            if (p.matches(node)) {
                if (match != null) {
                    throw new RuntimeException("BUG: Error in elementary pattern definition!\n"
                            + "Node " + node + " matched " + match + " and " + p + "!");
                }
                match = p;
            }
        }

        if (match == null) {
            throw new RuntimeException("BUG: Error in elementary pattern definition!\n"
                    + "Node " + node + " did not match any pattern!");
        }

        return match;
    }
}

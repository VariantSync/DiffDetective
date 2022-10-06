package org.variantsync.diffdetective.editclass;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;

import java.util.List;
import java.util.Map;

/**
 * Interface for custom catalogs of edit classes.
 * @author Paul Bittner
 */
public interface EditClassCatalogue {
    /**
     * Gives a list of all edit classes in this catalogue.
     * The list should be constant and immutable (i.e., the very same list is returned each time all is invoked).
     * The edit class should be immutable, too.
     * @return a constant list of all edit classes in this catalogue.
     */
    List<EditClass> all();

    /**
     * Returns a mapping from diff types to the edit classes that may match nodes of the given diff type.
     * The returned map as well as all its values should be immutable and constant.     *
     * @return A classification of edit classes by their diff types.
     */
    Map<DiffType, List<EditClass>> byType();

    /**
     * Returns the edit class that matches the given node.
     * Each node matches exactly one edit class.
     * @param node The node of which to find its edit class.
     * @return Returns the edit class that matches the given node.
     */
    default EditClass match(DiffNode node)
    {
        if (!node.isArtifact()) {
            throw new IllegalArgumentException("Expected an artifact node but got " + node.nodeType + "!");
        }

        final List<EditClass> classessToCheck = byType().get(node.diffType);

        EditClass match = null;
        for (final EditClass p : classessToCheck) {
            if (p.matches(node)) {
                if (match != null) {
                    throw new RuntimeException("BUG: Error in edit class definition!\n"
                            + "Node " + node + " matched " + match + " and " + p + "!");
                }
                match = p;
            }
        }

        if (match == null) {
            throw new RuntimeException("BUG: Error in edit class definition!\n"
                    + "Node " + node + " did not match any edit class!");
        }

        return match;
    }
}

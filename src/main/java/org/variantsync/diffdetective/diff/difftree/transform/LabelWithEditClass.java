package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;

import java.util.List;

/**
 * Label all nodes with their edit class.
 * This transformation leaves the graph structure of the {@link DiffTree} unchanged.
 * All {@link DiffNode#isArtifact() artifact} nodes will be labeled by their respective edit class.
 * All other nodes will be labeled by the {@link org.variantsync.diffdetective.diff.difftree.NodeType#name name of their node type}.
 * @author Paul Bittner
 */
public class LabelWithEditClass implements DiffTreeTransformer {
    private final DiffTreeTransformer relabelNodes;

    /**
     * Creates a new transformation that will use the given catalog of edit classes
     * to relabel {@link DiffNode#isArtifact() artifact} nodes.
     * @param editClasses Catalog of edit classes to match on artifact nodes.
     */
    public LabelWithEditClass(final EditClassCatalogue editClasses) {
        relabelNodes = new RelabelNodes(d -> {
            if (d.isArtifact()) {
                return editClasses.match(d).getName();
            } else {
                return d.nodeType.name;
            }
        });
    }

    @Override
    public void transform(DiffTree diffTree) {
        relabelNodes.transform(diffTree);
    }

    @Override
    public List<Class<? extends DiffTreeTransformer>> getDependencies() {
        return relabelNodes.getDependencies();
    }
}

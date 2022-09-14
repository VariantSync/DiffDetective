package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;

import java.util.List;

/**
 * Collapses edit classes in a DiffTree.
 * Contrary to its name, this transformation leaves a DiffTree's graph structure unchanged.
 * This transformation uses the {@link RelabelNodes} transformer to relabel all nodes.
 * All {@link DiffNode#isArtifact() artifact} nodes will be labeled by their respective edit class.
 * All other nodes will be labeled by the {@link org.variantsync.diffdetective.diff.difftree.NodeType#name name of their node type}.
 * @author Paul Bittner
 */
public class CollapseEditClasses implements DiffTreeTransformer {
    private final DiffTreeTransformer relabelNodes;

    /**
     * Creates a new transformation that will use the given catalog of edit classes
     * to relabel {@link DiffNode#isArtifact() artifact} nodes.
     * @param editClasses Catalog of edit classes to match on artifact nodes.
     */
    public CollapseEditClasses(final EditClassCatalogue editClasses) {
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

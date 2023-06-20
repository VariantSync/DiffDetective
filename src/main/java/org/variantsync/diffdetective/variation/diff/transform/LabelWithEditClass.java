package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.editclass.EditClassCatalogue;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.NodeType; // For Javadoc

import java.util.List;

/**
 * Label all nodes with their edit class.
 * This transformation leaves the graph structure of the {@link VariationDiff} unchanged.
 * All {@link DiffNode#isArtifact() artifact} nodes will be labeled by their respective edit class.
 * All other nodes will be labeled by the {@link NodeType#name name of their node type}.
 * @author Paul Bittner
 */
public class LabelWithEditClass implements VariationDiffTransformer<DiffLinesLabel> {
    private final VariationDiffTransformer<DiffLinesLabel> relabelNodes;

    /**
     * Creates a new transformation that will use the given catalog of edit classes
     * to relabel {@link DiffNode#isArtifact() artifact} nodes.
     * @param editClasses Catalog of edit classes to match on artifact nodes.
     */
    public LabelWithEditClass(final EditClassCatalogue editClasses) {
        relabelNodes = new RelabelNodes<DiffLinesLabel>(d -> {
            if (d.isArtifact()) {
                return DiffLinesLabel.ofCodeBlock(editClasses.match(d).getName());
            } else {
                return DiffLinesLabel.ofCodeBlock(d.getNodeType().name);
            }
        });
    }

    @Override
    public void transform(VariationDiff<DiffLinesLabel> variationDiff) {
        relabelNodes.transform(variationDiff);
    }

    @Override
    public List<Class<? extends VariationDiffTransformer<DiffLinesLabel>>> getDependencies() {
        return relabelNodes.getDependencies();
    }
}

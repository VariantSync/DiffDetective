package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPatternCatalogue;

import java.util.List;

/**
 * Collapses elementary patterns in a DiffTree.
 * Contrary to its name, this transformation leaves a DiffTree's graph structure unchanged.
 * This transformation uses the {@link RelabelNodes} transformer to relabel all nodes.
 * All {@link DiffNode#isCode() artifact} nodes will be labeled by their respective elementary pattern.
 * All other nodes will be labeled by the {@link org.variantsync.diffdetective.diff.difftree.CodeType#name name of their code type}.
 * @author Paul Bittner
 */
public class CollapseElementaryPatterns implements DiffTreeTransformer {
    private final DiffTreeTransformer relabelNodes;

    /**
     * Creates a new transformation that will use the given catalog of elementary patterns
     * to relabel {@link DiffNode#isCode() artifact} nodes.
     * @param patterns Catalog of patterns to match on artifact nodes.
     */
    public CollapseElementaryPatterns(final ElementaryPatternCatalogue patterns) {
        relabelNodes = new RelabelNodes(d -> {
            if (d.isCode()) {
                return patterns.match(d).getName();
            } else {
                return d.codeType.name;
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

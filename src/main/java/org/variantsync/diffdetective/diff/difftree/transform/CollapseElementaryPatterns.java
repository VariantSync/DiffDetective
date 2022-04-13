package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPatternCatalogue;

import java.util.List;

public class CollapseElementaryPatterns implements DiffTreeTransformer {
    private final DiffTreeTransformer relabelNodes;

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

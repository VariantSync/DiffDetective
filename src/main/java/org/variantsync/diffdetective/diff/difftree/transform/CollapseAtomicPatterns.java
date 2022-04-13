package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.pattern.atomic.AtomicPatternCatalogue;

import java.util.List;

public class CollapseAtomicPatterns implements DiffTreeTransformer {
    private final DiffTreeTransformer relabelNodes;

    public CollapseAtomicPatterns(final AtomicPatternCatalogue atomics) {
        relabelNodes = new RelabelNodes(d -> {
            if (d.isCode()) {
                return atomics.match(d).getName();
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

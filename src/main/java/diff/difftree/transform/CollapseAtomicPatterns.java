package diff.difftree.transform;

import diff.difftree.DiffTree;
import pattern.atomic.AtomicPatternCatalogue;

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

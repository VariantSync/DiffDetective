package diff.difftree.transform;

import diff.difftree.DiffTree;
import pattern.AtomicPattern;

import java.util.List;

public class CollapseAtomicPatterns implements DiffTreeTransformer {
    private final DiffTreeTransformer inner = new RelabelNodes(d -> {
        if (d.isCode()) {
            return AtomicPattern.getPattern(d).getName();
        } else {
            return d.codeType.name;
        }
    });

    @Override
    public void transform(DiffTree diffTree) {
        inner.transform(diffTree);
    }

    @Override
    public List<Class<? extends DiffTreeTransformer>> getDependencies() {
        return inner.getDependencies();
    }
}

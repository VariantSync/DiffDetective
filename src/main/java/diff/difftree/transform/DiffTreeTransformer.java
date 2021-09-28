package diff.difftree.transform;

import diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.List;

public interface DiffTreeTransformer {
    void transform(final DiffTree diffTree);

    default List<Class<? extends DiffTreeTransformer>> getDependencies() {
        return new ArrayList<>(0);
    }

    static void checkDependencies(final List<DiffTreeTransformer> transformers) {
        for (int i = transformers.size() - 1; i >= 0; --i) {
            final DiffTreeTransformer currentTransformer = transformers.get(i);
            final List<Class<? extends DiffTreeTransformer>> currentDependencies = currentTransformer.getDependencies();
            for (final Class<? extends DiffTreeTransformer> dependency : currentDependencies) {
                boolean dependencyMet = false;
                for (int j = i - 1; j >= 0; --j) {
                    if (dependency.isInstance(transformers.get(j))) {
                        dependencyMet = true;
                        break;
                    }
                }
                if (!dependencyMet) {
                    throw new RuntimeException("Dependency not met! DiffTreeTransformer "
                            + currentTransformer
                            + " requires a transformer of type "
                            + dependency
                            + " applied before!");
                }
            }
        }
    }

    static void apply(final List<DiffTreeTransformer> transformers, final DiffTree tree) {
        checkDependencies(transformers);
        for (final DiffTreeTransformer t : transformers) {
            t.transform(tree);
        }
    }
}

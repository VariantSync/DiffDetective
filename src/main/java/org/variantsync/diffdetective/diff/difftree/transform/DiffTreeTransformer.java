package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface that represents inplace transformations of DiffTrees.
 * A DiffTreeTransformer is intended to alter a given DiffTree.
 * @author Paul Bittner
 */
public interface DiffTreeTransformer {
    /**
     * Apply a transformation to the given DiffTree inplace.
     * The given tree will be changed.
     * @param diffTree The DiffTree to transform.
     */
    void transform(final DiffTree diffTree);

    /**
     * Returns a list of dependencies to other transformers.
     * A transformer should only be run, if another transformation with the respective type was run for each type on the dependencies.
     * @return List of types of which instances should be run before applying this transformation.
     */
    default List<Class<? extends DiffTreeTransformer>> getDependencies() {
        return new ArrayList<>(0);
    }

    /**
     * Checks that the dependencies of all given DiffTreeTransformers are satisfied when
     * applying the transformers sequentially.
     * @param transformers The transformers whose dependencies to check.
     * @throws RuntimeException when a dependency is not met.
     */
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

    /**
     * Applies all given transformers to the given DiffTree sequentially.
     * First checks that all dependencies between transformers are met via {@link #checkDependencies(List)}.
     * @param transformers Transformers to apply sequentially.
     * @param tree Tree to transform inplace.
     */
    static void apply(final List<DiffTreeTransformer> transformers, final DiffTree tree) {
        checkDependencies(transformers);
        for (final DiffTreeTransformer t : transformers) {
            t.transform(tree);
        }
    }
}

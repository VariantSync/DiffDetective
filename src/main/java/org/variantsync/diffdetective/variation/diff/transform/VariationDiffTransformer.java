package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface that represents inplace transformations of VariationDiffs.
 * A VariationDiffTransformer is intended to alter a given VariationDiff.
 * @author Paul Bittner
 */
public interface VariationDiffTransformer<L extends Label> {
    /**
     * Apply a transformation to the given VariationDiff inplace.
     * The given tree will be changed.
     * @param variationDiff The VariationDiff to transform.
     */
    void transform(final VariationDiff<L> variationDiff);

    /**
     * Returns a list of dependencies to other transformers.
     * A transformer should only be run, if another transformation with the respective type was run for each type on the dependencies.
     * @return List of types of which instances should be run before applying this transformation.
     */
    default List<Class<? extends VariationDiffTransformer<L>>> getDependencies() {
        return new ArrayList<>(0);
    }

    /**
     * Checks that the dependencies of all given VariationDiffTransformers are satisfied when
     * applying the transformers sequentially.
     * @param transformers The transformers whose dependencies to check.
     * @throws RuntimeException when a dependency is not met.
     */
    static <L extends Label> void checkDependencies(final List<VariationDiffTransformer<L>> transformers) {
        for (int i = transformers.size() - 1; i >= 0; --i) {
            final VariationDiffTransformer<L> currentTransformer = transformers.get(i);
            final List<Class<? extends VariationDiffTransformer<L>>> currentDependencies = currentTransformer.getDependencies();
            for (final Class<? extends VariationDiffTransformer<L>> dependency : currentDependencies) {
                boolean dependencyMet = false;
                for (int j = i - 1; j >= 0; --j) {
                    if (dependency.isInstance(transformers.get(j))) {
                        dependencyMet = true;
                        break;
                    }
                }
                if (!dependencyMet) {
                    throw new RuntimeException("Dependency not met! VariationDiffTransformer "
                            + currentTransformer
                            + " requires a transformer of type "
                            + dependency
                            + " applied before!");
                }
            }
        }
    }

    /**
     * Applies all given transformers to the given VariationDiff sequentially.
     * First checks that all dependencies between transformers are met via {@link #checkDependencies(List)}.
     * @param transformers Transformers to apply sequentially.
     * @param tree Tree to transform inplace.
     */
    static <L extends Label> void apply(final List<VariationDiffTransformer<L>> transformers, final VariationDiff<L> tree) {
        checkDependencies(transformers);
        for (final VariationDiffTransformer<L> t : transformers) {
            t.transform(tree);
        }
    }
}

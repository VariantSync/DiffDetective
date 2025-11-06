package org.variantsync.diffdetective.variation.diff.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Models an operation that changes an object of type T inplace (i.e., the object is altered).
 * To model further assumptions on the given elements T (i.e., assumptions that cannot easily be expressed as a type),
 * Transformers may have dependencies to other transformers, which should be applied first.
 */
public interface Transformer<T> {
    /**
     * Apply a transformation to the given Object inplace.
     * The object will be changed.
     * @param element The T object to transform.
     */
    void transform(final T element);

    /**
     * Returns a list of dependencies to other transformers.
     * A transformer should only be run, if another transformation with the respective type was run for each type on the dependencies.
     * @return List of types of which instances should be run before applying this transformation.
     */
    default List<Class<? extends Transformer<T>>> getDependencies() {
        return new ArrayList<>(0);
    }

    /**
     * Checks that the dependencies of all given transformers are satisfied when
     * applying the transformers sequentially.
     * @param transformers The transformers whose dependencies to check.
     * @throws RuntimeException when a dependency is not met.
     */
    static <T> void checkDependencies(final List<? extends Transformer<T>> transformers) {
        for (int i = transformers.size() - 1; i >= 0; --i) {
            final Transformer<T> currentTransformer = transformers.get(i);
            final List<Class<? extends Transformer<T>>> currentDependencies = currentTransformer.getDependencies();
            for (final Class<? extends Transformer<T>> dependency : currentDependencies) {
                boolean dependencyMet = false;
                for (int j = i - 1; j >= 0; --j) {
                    if (dependency.isInstance(transformers.get(j))) {
                        dependencyMet = true;
                        break;
                    }
                }
                if (!dependencyMet) {
                    throw new RuntimeException("Dependency not met! Transformer "
                            + currentTransformer
                            + " requires a transformer of type "
                            + dependency
                            + " applied before!");
                }
            }
        }
    }

    /**
     * Applies all given transformers to the given element sequentially.
     * First checks that all dependencies between transformers are met via {@link #checkDependencies(List)}.
     * @param transformers Transformers to apply sequentially.
     * @param element Tree to transform inplace.
     */
    static <T> void apply(final List<? extends Transformer<T>> transformers, final T element) {
        checkDependencies(transformers);
        for (final Transformer<T> t : transformers) {
            t.transform(element);
        }
    }

}

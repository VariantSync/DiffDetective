package org.variantsync.diffdetective.preliminary.pattern;

/**
 * Abstract class for edit patterns.
 * A pattern has a unique name and may be matched on domain elements of a given type.
 * @param <E> Type of elements on which the pattern may be matched.
 * @author Sören Viegener, Paul Bittner
 */
public abstract class Pattern<E> {
    /**
     * The name that uniquely identifies this pattern.
     */
    protected final String name;

    /**
     * Create a new pattern with the given name.
     * @param name Unique identifier.
     */
    public Pattern(final String name) {
        this.name = name;
    }

    /**
     * Returns true iff the given domain element matches this pattern.
     */
    public abstract boolean matches(E e);

    /**
     * Returns the name of this pattern.
     */
    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }
}

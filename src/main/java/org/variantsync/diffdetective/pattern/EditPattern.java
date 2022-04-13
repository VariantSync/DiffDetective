package org.variantsync.diffdetective.pattern;

public abstract class EditPattern<E> {
    protected String name;

    public EditPattern(final String name) {
        this.name = name;
    }

    public abstract boolean matches(E e);

    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }
}

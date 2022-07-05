package org.variantsync.diffdetective.tablegen;

/** Horizontal column alignments of a LaTex table. */
public enum Alignment {
    LEFT("l"),
    LEFT_DASH("l |"),
    CENTER("c"),
    RIGHT("r"),
    RIGHT_DASH("r |"),
    DASH_RIGHT("| r");

    private final String val;

    Alignment(String val) {
        this.val = val;
    }

    /** Returns the string used in LaTex table specifications. */
    @Override
    public String toString() {
        return val;
    }
}

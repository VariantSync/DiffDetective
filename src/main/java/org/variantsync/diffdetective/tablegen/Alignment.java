package org.variantsync.diffdetective.tablegen;

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

    @Override
    public String toString() {
        return val;
    }
}

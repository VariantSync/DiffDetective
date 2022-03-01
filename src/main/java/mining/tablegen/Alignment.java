package mining.tablegen;

public enum Alignment {
    LEFT("l"),
    CENTER("c"),
    RIGHT("r"),
    RIGHT_DASH("r |");

    private String val;

    Alignment(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}

package diff;

/**
 * Class to hold a range of line numbers.
 * Mainly used to locate code snippets in source code files and textual diffs.
 */
public class Lines {
    private int fromInclusive; // including
    private int toExclusive; // exluding

    private Lines(int fromInclusive, int toExclusive) {
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
    }

    public static Lines Invalid() {
        return new Lines(-1, -1);
    }

    public static Lines SingleLine(int lineNo) {
        return FromInclToIncl(lineNo, lineNo);
    }

    public static Lines FromInclToExcl(int fromInclusive, int toExclusive) {
        return new Lines(fromInclusive, toExclusive);
    }

    public static Lines FromInclToIncl(int fromInclusive, int toInclusive) {
        return new Lines(fromInclusive, toInclusive + 1);
    }

    public void set(final Lines other) {
        this.fromInclusive = other.fromInclusive;
        this.toExclusive = other.toExclusive;
    }

    public void setFromInclusive(int fromInclusive) {
        this.fromInclusive = fromInclusive;
    }

    public void setToExclusive(int toExclusive) {
        this.toExclusive = toExclusive;
    }

    public void setToInclusive(int toInclusive) {
        this.toExclusive = toInclusive + 1;
    }

    public int getFromInclusive() {
        return fromInclusive;
    }

    public int getToExclusive() {
        return toExclusive - 1;
    }

//    public int getToInclusive() {
//        return toExclusive;
//    }
}

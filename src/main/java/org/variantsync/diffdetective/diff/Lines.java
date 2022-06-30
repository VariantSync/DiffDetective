package org.variantsync.diffdetective.diff;

/**
 * Class to hold a range of line numbers.
 * Mainly used to locate code snippets in source code files and textual diffs.
 * @author Paul Bittner
 */
public class Lines {
    private final int fromInclusive; // including
    private final int toExclusive; // excluding

    private Lines(int fromInclusive, int toExclusive) {
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
    }

    /**
     * Creates an invalid range that does not represent a valid range of line numbers in a text file.
     */
    public static Lines Invalid() {
        return new Lines(-1, -1);
    }

    /**
     * Creates a range that covers only a single line.
     * @param lineNo The line number to range over.
     */
    public static Lines SingleLine(int lineNo) {
        return FromInclToIncl(lineNo, lineNo);
    }

    /**
     * Creates a range of line numbers including the first line number and ending at but excluding the second line number.
     * @param fromInclusive Start of the range.
     * @param toExclusive Line number after the end of the range.
     */
    public static Lines FromInclToExcl(int fromInclusive, int toExclusive) {
        return new Lines(fromInclusive, toExclusive);
    }

    /**
     * Creates a range of line numbers including the first line number and ending at and including the second line number.
     * @param fromInclusive Start of the range.
     * @param toInclusive End of the range.
     */
    public static Lines FromInclToIncl(int fromInclusive, int toInclusive) {
        return new Lines(fromInclusive, toInclusive + 1);
    }

    /**
     * Returns the starting line number of this range.
     * This number is included within the range.
     */
    public int getFromInclusive() {
        return fromInclusive;
    }

    /**
     * Returns the ending line number of this range.
     * This number is excluded from the range (i.e., it points to the line right after the last line).
     */
    public int getToExclusive() {
        return toExclusive;
    }

//    public int getToInclusive() {
//        return toExclusive;
//    }


    @Override
    public String toString() {
        return "[" + fromInclusive + ", " + toExclusive + ")";
    }
}

package org.variantsync.diffdetective.util;

/**
 * Class to hold a range of line numbers.
 * Mainly used to locate code snippets in source code files and textual diffs.
 * @param fromInclusive The starting line number of this range. This number is included within the range.
 * @param toExclusive The ending line number of this range. This number is excluded from the range
 *                    (i.e., it points to the line right after the last line).
 * @author Paul Bittner
 */
public record LineRange(
        int fromInclusive,
        int toExclusive
) {
    /**
     * Creates an invalid range that does not represent a valid range of line numbers in a text file.
     */
    public static LineRange Invalid() {
        return new LineRange(-1, -1);
    }

    /**
     * Creates a range that covers only a single line.
     * @param lineNo The line number to range over.
     */
    public static LineRange SingleLine(int lineNo) {
        return FromInclToIncl(lineNo, lineNo);
    }

    /**
     * Creates a range of line numbers including the first line number and ending at but excluding the second line number.
     * @param fromInclusive Start of the range.
     * @param toExclusive Line number after the end of the range.
     */
    public static LineRange FromInclToExcl(int fromInclusive, int toExclusive) {
        return new LineRange(fromInclusive, toExclusive);
    }

    /**
     * Creates a range of line numbers including the first line number and ending at and including the second line number.
     * @param fromInclusive Start of the range.
     * @param toInclusive End of the range.
     */
    public static LineRange FromInclToIncl(int fromInclusive, int toInclusive) {
        return new LineRange(fromInclusive, toInclusive + 1);
    }


    @Override
    public String toString() {
        return "[" + fromInclusive + ", " + toExclusive + ")";
    }
}

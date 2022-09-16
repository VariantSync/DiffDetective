package org.variantsync.diffdetective.util;

import java.util.Collection;
import java.util.regex.Pattern;

/** A collection of useful utilities related to string processing. */
public class StringUtils {
    /** An operating system independent line break used in almost all internal strings. */
    public final static String LINEBREAK = "\r\n";
    /** A regex to identify line breaks of any operating system .*/
    public final static Pattern LINEBREAK_REGEX = Pattern.compile("\\r\\n|\\r|\\n");

    /** Remove the content {@code builder} so it can be reused for a new string. */
    public static void clear(final StringBuilder builder) {
        // According to https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
        builder.setLength(0);
    }

    /**
     * Append a human readable string representation of {@code collection} to {@code b}.
     *
     * @param indent an indent prepended to all appended lines
     * @param b a string builder on which result is appended
     * @param os the collection to be pretty printed
     */
    private static void prettyPrintNestedCollections(final String indent, final StringBuilder b, final Collection<?> os) {
        b.append(indent).append("[").append(LINEBREAK);
        final String childIndent = "  " + indent;
        for (final Object o : os) {
            if (o instanceof Collection<?> oos) {
                prettyPrintNestedCollections(childIndent, b, oos);
            } else {
                b.append(childIndent).append(o.toString());
            }

            b.append(",").append(LINEBREAK);
        }
        b.append(indent).append("]");
    }

    /** Returns a human readable string representation of {@code collection}. */
    public static String prettyPrintNestedCollections(final Collection<?> collection) {
        final StringBuilder b = new StringBuilder();
        prettyPrintNestedCollections("", b, collection);
        return b.toString();
    }
}

package org.variantsync.diffdetective.util;

import java.util.Collection;

public class StringUtils {
    public final static String LINEBREAK = "\r\n";
    public final static String LINEBREAK_REGEX = "\\r?\\n";

    public static void clear(final StringBuilder builder) {
        // According to https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
        builder.setLength(0);
    }

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

    public static String prettyPrintNestedCollections(final Collection<?> collection) {
        final StringBuilder b = new StringBuilder();
        prettyPrintNestedCollections("", b, collection);
        return b.toString();
    }
}

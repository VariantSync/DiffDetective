package org.variantsync.diffdetective.util;

/** Commonly used constants used in LaTex code. */
public final class LaTeX {
    /** Delimiter between LaTex table columns. */
    public static final String TABLE_SEPARATOR = " & ";
    /** Delimiter between LaTex table rows. */
    public static final String TABLE_ENDROW = "\\\\" + StringUtils.LINEBREAK;

    /**
     * Characters suitable for use with the {@code \verb} command. In decreasing preference.
     */
    private static final char[] verbDelimiters = "|/!?+\"'-_=~#@$&^:;.,".toCharArray();

    /**
     * Wraps {@code text} into a {@code \verb} command.
     * Automatically tries to select a suitable delimiter not contained in {@code text}.
     *
     * @param text a string which should appear verbatim in LaTeX output
     * @return LaTeX source code producing {@code text} verbatim
     * @throws IllegalArgumentException if no suitable delimiter could be found.
     */
    public static String escape(String text) {
        for (char verbChar : verbDelimiters) {
            if (!text.contains(String.valueOf(verbChar))) {
                return "\\verb" + verbChar + text + verbChar;
            }
        }

        throw new IllegalArgumentException("No suitable LaTeX escape character found for the string: " + text);
    }
}

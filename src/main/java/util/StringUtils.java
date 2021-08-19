package util;

public class StringUtils {
    public final static String LINEBREAK = "\r\n";

    public static void clear(final StringBuilder builder) {
        // According to https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
        builder.setLength(0);
    }
}

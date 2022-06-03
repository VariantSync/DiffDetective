package org.variantsync.diffdetective.diff.difftree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum CodeType {
    IF("if"),
    ENDIF("endif"),
    ELSE("else"),
    ELIF("elif"),
    CODE("code"),
    ROOT("ROOT");

    public final String name;
    CodeType(String name) {
        this.name = name;
    }

    public boolean isConditionalMacro() {
        return this == IF || this == ELIF;
    }
    public boolean isMacro() {
        return this != ROOT && this != CODE;
    }

    final static Pattern regex = Pattern.compile("^[+-]?\\s*#\\s*(if|endif|else|elif)");

    public static CodeType ofDiffLine(String line) {
        Matcher matcher = regex.matcher(line);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (id.equals(IF.name)) {
                return IF;
            } else if (id.equals(ENDIF.name)) {
                return ENDIF;
            } else if (id.equals(ELSE.name)) {
                return ELSE;
            } else if (id.equals(ELIF.name)) {
                return ELIF;
            }
        }

        return CODE;
    }

    public static CodeType fromName(final String name) {
        for (CodeType candidate : values()) {
            if (candidate.toString().equalsIgnoreCase(name)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Given string \"" + name + "\" is not the name of a CodeType.");
    }

    public String asMacroText() {
        return "#" + this.name;
    }
}

package diff.difftree;

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

    final static String ifRegex = "^[+-]?\\s*#\\s*if.*$";
    final static String endifRegex = "^[+-]?\\s*#\\s*endif.*$";
    final static String elseRegex = "^[+-]?\\s*#\\s*else.*$";
    final static String elifRegex = "^[+-]?\\s*#\\s*elif.*$";

    public static CodeType ofDiffLine(String line) {
        if (line.matches(ifRegex)) {
            return IF;
        } else if (line.matches(endifRegex)) {
            return ENDIF;
        } else if (line.matches(elseRegex)) {
            return ELSE;
        } else if (line.matches(elifRegex)) {
            return ELIF;
        } else {
            return CODE;
        }
    }
}

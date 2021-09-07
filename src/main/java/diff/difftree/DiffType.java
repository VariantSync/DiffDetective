package diff.difftree;

public enum DiffType {
    ADD("+"),
    REM("-"),
    NON(" ");

    public final String name;

    DiffType(String name) {
        this.name = name;
    }

    final static String addCharacter = "+";
    final static String remCharacter = "-";

    public static DiffType ofDiffLine(String line) {
        if (line.startsWith(addCharacter)) {
            return ADD;
        } else if (line.startsWith(remCharacter)) {
            return REM;
        } else {
            return NON;
        }
    }
}

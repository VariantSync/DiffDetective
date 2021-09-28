package diff.difftree.parse;

public enum ParseResultType {
    /**
     * A token (or LOC) could be parsed successfully and was consumed.
     */
    Success,
    /**
     * The parser was not responsible for parsing a token (or LOC) and thus did neither parse
     * nor consume the given token. Another parser should be used.
     */
    NotMyDuty,
    /**
     * A token (or LOC) should have been parsed but parsing failed (e.g., because of an ill-formed string).
     */
    Error
}

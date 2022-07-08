package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.result.DiffError;

import java.util.function.BiConsumer;

/**
 * Result type for parsing.
 * Mostly used for parsing multiline macros.
 * @param type Decides if an element was parsed successfully or not .
 * @param errorType An error description in case an error occurred.
 * @param message A custom message.
 * @see MultiLineMacroParser
 * @author Paul Bittner
 */
public record ParseResult(ParseResultType type, DiffError errorType, String message) {
    /**
     * Default value for returning that parsing was successful.
     */
    public static final ParseResult SUCCESS = new ParseResult(ParseResultType.Success);

    /**
     * Default value for returning that a token was not parsed because
     * it was not relevant for a certain parser.
     * This is mostly used by the {@link MultiLineMacroParser} when it finds a
     * source code line that is not part of a multiline macro definition.
     */
    public static final ParseResult NOT_MY_DUTY = new ParseResult(ParseResultType.NotMyDuty);

    /**
     * Creates an error result.
     * @param errorType The type of error that occurred.
     * @param message A custom error message.
     * @return An error result.
     */
    public static ParseResult ERROR(final DiffError errorType, final String message) {
        return new ParseResult(ParseResultType.Error, errorType, message);
    }

    /**
     * The same as {@link ParseResult#ERROR(DiffError, String)} but uses the message stored in the given
     * error as the custom message (i.e., {@link DiffError#id()}).
     */
    public static ParseResult ERROR(final DiffError errorType) {
        return ERROR(errorType, errorType.id());
    }

    private ParseResult(ParseResultType type) {
        this(type, null, "");
    }

    /**
     * Returns true if this result represents an error.
     */
    public boolean isError() {
        return type == ParseResultType.Error;
    }

    /**
     * Invokes the given function with the respective error if this
     * result is an error.
     * @param handler Callback to invoke in case an error occured.
     * @return True iff this result is an error.
     * @see ParseResult#isError()
     */
    public boolean onError(BiConsumer<DiffError, String> handler) {
        if (isError()) {
            handler.accept(errorType, message);
            return true;
        }

        return false;
    }
}

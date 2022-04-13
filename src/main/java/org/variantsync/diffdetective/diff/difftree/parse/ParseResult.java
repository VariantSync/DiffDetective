package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.result.DiffError;

import java.util.function.BiConsumer;

public record ParseResult(ParseResultType type, DiffError errorType, String message) {
    public static final ParseResult SUCCESS = new ParseResult(ParseResultType.Success);
    public static final ParseResult NOT_MY_DUTY = new ParseResult(ParseResultType.NotMyDuty);
    public static ParseResult ERROR(final DiffError errorType, final String message) {
        return new ParseResult(ParseResultType.Error, errorType, message);
    }
    public static ParseResult ERROR(final DiffError errorType) {
        return ERROR(errorType, errorType.id());
    }

    private ParseResult(ParseResultType type) {
        this(type, null, "");
    }

    public boolean isError() {
        return type == ParseResultType.Error;
    }

    public boolean onError(BiConsumer<DiffError, String> handler) {
        if (isError()) {
            handler.accept(errorType, message);
            return true;
        }

        return false;
    }
}

package diff.data.difftreeparse;

import java.util.function.Consumer;

public record ParseResult(ParseResultType type, String message) {
    public static final ParseResult SUCCESS = new ParseResult(ParseResultType.Success);
    public static final ParseResult NOT_MY_DUTY = new ParseResult(ParseResultType.NotMyDuty);
    public static ParseResult ERROR(final String message) {
        return new ParseResult(ParseResultType.Error, message);
    }

    private ParseResult(ParseResultType type) {
        this(type, "");
    }

    public boolean isError() {
        return type == ParseResultType.Error;
    }

    public boolean onError(Consumer<String> handler) {
        if (isError()) {
            handler.accept(message);
            return true;
        }

        return false;
    }
}

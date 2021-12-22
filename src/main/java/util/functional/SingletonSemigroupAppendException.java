package util.functional;

public class SingletonSemigroupAppendException extends RuntimeException {
    public SingletonSemigroupAppendException(Object me, Object other) {
        super("Value \"" + other + "\" to append does not equal singleton \"" + me + "\"!");
    }
}

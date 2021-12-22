package util;

public class Util {
    @SuppressWarnings("unchecked")
    public static <From, To> To cast(From f) throws ClassCastException {
        return (To) f;
    }
}

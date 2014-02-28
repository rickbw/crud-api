package rickbw.crud.util;


/**
 * Patterned after Guava's class of the same name.
 */
public final class Preconditions {

    public static <T> T checkNotNull(final T value) {
        if (null == value) {
            throw new NullPointerException();
        }
        return value;
    }

    public static <T> T checkNotNull(final T value, final String message) {
        if (null == value) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static void checkArgument(final boolean value, final String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    private Preconditions() {
        // prevent instantiation
    }

}

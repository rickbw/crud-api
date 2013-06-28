package rickbw.crud;

import com.google.common.base.Function;


/**
 * A utility class for working with {@link ResourceConsumer}s.
 */
public final class ResourceConsumers {

    /**
     * Values will be transformed with the given {@link Function}.
     */
    public static <FROM, TO> ResourceConsumer<FROM> adapt(
            final ResourceConsumer<? super TO> consumer,
            final Function<? super FROM, ? extends TO> function) {
        return new ResourceConsumer<FROM>() {
            @Override
            public void accept(final FROM value) {
                final TO transformed = function.apply(value);
                consumer.accept(transformed);
            }
        };
    }

    private ResourceConsumers() {
        // no external construction
    }

}

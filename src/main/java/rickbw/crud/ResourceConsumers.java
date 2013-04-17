package rickbw.crud;

import com.google.common.base.Function;


/**
 * A utility class for working with {@link ResourceConsumer}s and
 * {@link MapResourceConsumer}s.
 */
public final class ResourceConsumers {

    /**
     * Every invocation will use the given key.
     */
    public static <KEY, RSRC> ResourceConsumer<RSRC> toConsumer(
            final MapResourceConsumer<? super KEY, ? super RSRC> consumer,
            final KEY key) {
        return new ResourceConsumer<RSRC>() {
            @Override
            public void accept(final RSRC value) {
                consumer.accept(key, value);
            }
        };
    }

    /**
     * All keys will be ignored.
     */
    public static <KEY, RSRC> MapResourceConsumer<KEY, RSRC> toMapConsumer(
            final ResourceConsumer<? super RSRC> consumer) {
        return new MapResourceConsumer<KEY, RSRC>() {
            @Override
            public void accept(final KEY key, final RSRC value) {
                consumer.accept(value);
            }
        };
    }

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

    /**
     * Keys will be transformed with the given {@link Function}.
     */
    public static <FROMKEY, TOKEY, RSRC> MapResourceConsumer<FROMKEY, RSRC> adaptKey(
            final MapResourceConsumer<? super TOKEY, ? super RSRC> consumer,
            final Function<? super FROMKEY, ? extends TOKEY> function) {
        return new MapResourceConsumer<FROMKEY, RSRC>() {
            @Override
            public void accept(final FROMKEY key, final RSRC value) {
                final TOKEY transformed = function.apply(key);
                consumer.accept(transformed, value);
            }
        };
    }

    /**
     * Values will be transformed with the given {@link Function}.
     */
    public static <KEY, FROM, TO> MapResourceConsumer<KEY, FROM> adaptValue(
            final MapResourceConsumer<? super KEY, ? super TO> consumer,
            final Function<? super FROM, ? extends TO> function) {
        return new MapResourceConsumer<KEY, FROM>() {
            @Override
            public void accept(final KEY key, final FROM value) {
                final TO transformed = function.apply(value);
                consumer.accept(key, transformed);
            }
        };
    }

    /**
     * Keys and values will be transformed with the given {@link Function}s.
     */
    public static <FROMKEY, TOKEY, FROM, TO> MapResourceConsumer<FROMKEY, FROM> adapt(
            final MapResourceConsumer<? super TOKEY, ? super TO> consumer,
            final Function<? super FROMKEY, ? extends TOKEY> keyFunction,
            final Function<? super FROM, ? extends TO> valueFunction) {
        return new MapResourceConsumer<FROMKEY, FROM>() {
            @Override
            public void accept(final FROMKEY key, final FROM value) {
                final TOKEY transformedKey = keyFunction.apply(key);
                final TO transformedValue = valueFunction.apply(value);
                consumer.accept(transformedKey, transformedValue);
            }
        };
    }

    private ResourceConsumers() {
        // no external construction
    }

}

package rickbw.crud.util;

import com.google.common.base.Preconditions;

import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rx.util.functions.Func1;


public final class ReadableResourceProviders {

    public static <KEY, FROM, TO> ReadableResourceProvider<KEY, TO> map(
            final ReadableResourceProvider<? super KEY, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final ReadableResourceProvider<KEY, TO> result = new ReadableResourceProvider<KEY, TO>() {
            @Override
            public ReadableResource<TO> get(final KEY key) {
                final ReadableResource<? extends FROM> resource = provider.get(key);
                final ReadableResource<TO> mapped = FluentReadableResource.from(resource).mapValue(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <FROM, TO, RESPONSE> ReadableResourceProvider<TO, RESPONSE> adaptKey(
            final ReadableResourceProvider<? super FROM, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final ReadableResourceProvider<TO, RESPONSE> result = new ReadableResourceProvider<TO, RESPONSE>() {
            @Override
            public ReadableResource<RESPONSE> get(final TO key) {
                final FROM transformedKey = adapter.call(key);
                final ReadableResource<RESPONSE> resource = provider.get(transformedKey);
                return resource;
            }
        };
        return result;
    }

    private ReadableResourceProviders() {
        // prevent instantiation
    }

}

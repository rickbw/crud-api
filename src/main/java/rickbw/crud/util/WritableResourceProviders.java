package rickbw.crud.util;

import com.google.common.base.Preconditions;

import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;
import rx.util.functions.Func1;


public final class WritableResourceProviders {

    public static <KEY, RSRC, FROM, TO> WritableResourceProvider<KEY, RSRC, TO> map(
            final WritableResourceProvider<? super KEY, ? super RSRC, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final WritableResourceProvider<KEY, RSRC, TO> result = new WritableResourceProvider<KEY, RSRC, TO>() {
            @Override
            public WritableResource<RSRC, TO> get(final KEY key) {
                final WritableResource<? super RSRC, ? extends FROM> resource = provider.get(key);
                final WritableResource<RSRC, TO> mapped = FluentWritableResource.from(resource).mapResponse(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <KEY, FROM, TO, RESPONSE> WritableResourceProvider<KEY, TO, RESPONSE> adaptNewValue(
            final WritableResourceProvider<? super KEY, ? super FROM, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final WritableResourceProvider<KEY, TO, RESPONSE> result = new WritableResourceProvider<KEY, TO, RESPONSE>() {
            @Override
            public WritableResource<TO, RESPONSE> get(final KEY key) {
                final WritableResource<? super FROM, RESPONSE> resource = provider.get(key);
                final WritableResource<TO, RESPONSE> transformed = FluentWritableResource.from(resource).adaptNewValue(adapter);
                return transformed;
            }
        };
        return result;
    }

    public static <FROM, TO, RSRC, RESPONSE> WritableResourceProvider<TO, RSRC, RESPONSE> adaptKey(
            final WritableResourceProvider<? super FROM, RSRC, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final WritableResourceProvider<TO, RSRC, RESPONSE> result = new WritableResourceProvider<TO, RSRC, RESPONSE>() {
            @Override
            public WritableResource<RSRC, RESPONSE> get(final TO key) {
                final FROM transformedKey = adapter.call(key);
                final WritableResource<RSRC, RESPONSE> resource = provider.get(transformedKey);
                return resource;
            }
        };
        return result;
    }

    private WritableResourceProviders() {
        // prevent instantiation
    }

}

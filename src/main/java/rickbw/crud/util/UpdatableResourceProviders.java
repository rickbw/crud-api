package rickbw.crud.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.UpdatableResource;
import rickbw.crud.UpdatableResourceProvider;
import rickbw.crud.util.rx.GuavaToRxFunction;
import rx.util.functions.Func1;


public final class UpdatableResourceProviders {

    public static <KEY, UPDATE, FROM, TO> UpdatableResourceProvider<KEY, UPDATE, TO> map(
            final UpdatableResourceProvider<? super KEY, ? super UPDATE, ? extends FROM> provider,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return map(provider, rxFunc);
    }

    public static <KEY, UPDATE, FROM, TO> UpdatableResourceProvider<KEY, UPDATE, TO> map(
            final UpdatableResourceProvider<? super KEY, ? super UPDATE, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final UpdatableResourceProvider<KEY, UPDATE, TO> result = new UpdatableResourceProvider<KEY, UPDATE, TO>() {
            @Override
            public UpdatableResource<UPDATE, TO> get(final KEY key) {
                final UpdatableResource<? super UPDATE, ? extends FROM> resource = provider.get(key);
                final UpdatableResource<UPDATE, TO> mapped = UpdatableResources.mapResponse(resource, mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <KEY, FROM, TO, RESPONSE> UpdatableResourceProvider<KEY, TO, RESPONSE> adaptUpdate(
            final UpdatableResourceProvider<? super KEY, ? super FROM, RESPONSE> provider,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptUpdate(provider, rxFunc);
    }

    public static <KEY, FROM, TO, RESPONSE> UpdatableResourceProvider<KEY, TO, RESPONSE> adaptUpdate(
            final UpdatableResourceProvider<? super KEY, ? super FROM, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final UpdatableResourceProvider<KEY, TO, RESPONSE> result = new UpdatableResourceProvider<KEY, TO, RESPONSE>() {
            @Override
            public UpdatableResource<TO, RESPONSE> get(final KEY key) {
                final UpdatableResource<? super FROM, RESPONSE> resource = provider.get(key);
                final UpdatableResource<TO, RESPONSE> transformed = UpdatableResources.adaptUpdate(resource, adapter);
                return transformed;
            }
        };
        return result;
    }

    public static <FROM, TO, UPDATE, RESPONSE> UpdatableResourceProvider<TO, UPDATE, RESPONSE> adaptKey(
            final UpdatableResourceProvider<? super FROM, UPDATE, RESPONSE> provider,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptKey(provider, rxFunc);
    }

    public static <FROM, TO, UPDATE, RESPONSE> UpdatableResourceProvider<TO, UPDATE, RESPONSE> adaptKey(
            final UpdatableResourceProvider<? super FROM, UPDATE, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final UpdatableResourceProvider<TO, UPDATE, RESPONSE> result = new UpdatableResourceProvider<TO, UPDATE, RESPONSE>() {
            @Override
            public UpdatableResource<UPDATE, RESPONSE> get(final TO key) {
                final FROM transformedKey = adapter.call(key);
                final UpdatableResource<UPDATE, RESPONSE> resource = provider.get(transformedKey);
                return resource;
            }
        };
        return result;
    }

    private UpdatableResourceProviders() {
        // prevent instantiation
    }

}

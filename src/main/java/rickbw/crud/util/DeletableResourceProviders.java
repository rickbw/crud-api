package rickbw.crud.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rickbw.crud.util.rx.GuavaToRxFunction;
import rx.util.functions.Func1;


public final class DeletableResourceProviders {

    public static <KEY, FROM, TO> DeletableResourceProvider<KEY, TO> map(
            final DeletableResourceProvider<? super KEY, ? extends FROM> provider,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return map(provider, rxFunc);
    }

    public static <KEY, FROM, TO> DeletableResourceProvider<KEY, TO> map(
            final DeletableResourceProvider<? super KEY, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final DeletableResourceProvider<KEY, TO> result = new DeletableResourceProvider<KEY, TO>() {
            @Override
            public DeletableResource<TO> get(final KEY key) {
                final DeletableResource<? extends FROM> resource = provider.get(key);
                final DeletableResource<TO> mapped = DeletableResources.mapResponse(resource, mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <FROM, TO, RESPONSE> DeletableResourceProvider<TO, RESPONSE> adaptKey(
            final DeletableResourceProvider<? super FROM, RESPONSE> provider,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptKey(provider, rxFunc);
    }

    public static <FROM, TO, RESPONSE> DeletableResourceProvider<TO, RESPONSE> adaptKey(
            final DeletableResourceProvider<? super FROM, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final DeletableResourceProvider<TO, RESPONSE> result = new DeletableResourceProvider<TO, RESPONSE>() {
            @Override
            public DeletableResource<RESPONSE> get(final TO key) {
                final FROM transformedKey = adapter.call(key);
                final DeletableResource<RESPONSE> resource = provider.get(transformedKey);
                return resource;
            }
        };
        return result;
    }

    private DeletableResourceProviders() {
        // prevent instantiation
    }

}

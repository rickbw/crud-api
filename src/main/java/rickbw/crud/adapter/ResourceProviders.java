package rickbw.crud.adapter;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rickbw.crud.UpdatableResource;
import rickbw.crud.UpdatableResourceProvider;
import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;
import rx.util.functions.Func1;


public final class ResourceProviders {

    // --- ReadableResources: ------------------------------------------------

    public static <KEY, FROM, TO> ReadableResourceProvider<KEY, TO> map(
            final ReadableResourceProvider<? super KEY, ? extends FROM> provider,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return map(provider, rxFunc);
    }

    public static <KEY, FROM, TO> ReadableResourceProvider<KEY, TO> map(
            final ReadableResourceProvider<? super KEY, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final ReadableResourceProvider<KEY, TO> result = new ReadableResourceProvider<KEY, TO>() {
            @Override
            public ReadableResource<TO> get(final KEY key) {
                final ReadableResource<? extends FROM> resource = provider.get(key);
                final ReadableResource<TO> mapped = Resources.mapValue(resource, mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <FROM, TO, RESPONSE> ReadableResourceProvider<TO, RESPONSE> adaptKey(
            final ReadableResourceProvider<? super FROM, RESPONSE> provider,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptKey(provider, rxFunc);
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


    // --- DeletableResources: -----------------------------------------------

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
                final DeletableResource<TO> mapped = Resources.mapResponse(resource, mapper);
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


    // --- WritableResources: ------------------------------------------------

    public static <KEY, RSRC, FROM, TO> WritableResourceProvider<KEY, RSRC, TO> map(
            final WritableResourceProvider<? super KEY, ? super RSRC, ? extends FROM> provider,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return map(provider, rxFunc);
    }

    public static <KEY, RSRC, FROM, TO> WritableResourceProvider<KEY, RSRC, TO> map(
            final WritableResourceProvider<? super KEY, ? super RSRC, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final WritableResourceProvider<KEY, RSRC, TO> result = new WritableResourceProvider<KEY, RSRC, TO>() {
            @Override
            public WritableResource<RSRC, TO> get(final KEY key) {
                final WritableResource<? super RSRC, ? extends FROM> resource = provider.get(key);
                final WritableResource<RSRC, TO> mapped = Resources.mapResponse(resource, mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <KEY, FROM, TO, RESPONSE> WritableResourceProvider<KEY, TO, RESPONSE> adaptNewValue(
            final WritableResourceProvider<? super KEY, ? super FROM, RESPONSE> provider,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptNewValue(provider, rxFunc);
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
                final WritableResource<TO, RESPONSE> transformed = Resources.adaptNewValue(resource, adapter);
                return transformed;
            }
        };
        return result;
    }

    public static <FROM, TO, RSRC, RESPONSE> WritableResourceProvider<TO, RSRC, RESPONSE> adaptKey(
            final WritableResourceProvider<? super FROM, RSRC, RESPONSE> provider,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptKey(provider, rxFunc);
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


    // --- UpdatableResources: -----------------------------------------------

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
                final UpdatableResource<UPDATE, TO> mapped = Resources.map(resource, mapper);
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
                final UpdatableResource<TO, RESPONSE> transformed = Resources.adaptUpdate(resource, adapter);
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


    // -----------------------------------------------------------------------

    private ResourceProviders() {
        // prevent instantiation
    }

}

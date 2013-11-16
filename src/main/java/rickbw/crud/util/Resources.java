package rickbw.crud.util;

import javax.annotation.Nullable;

import rickbw.crud.DeletableResource;
import rickbw.crud.ReadableResource;
import rickbw.crud.UpdatableResource;
import rickbw.crud.WritableResource;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.util.rx.GuavaToRxFunction;
import rx.Observable;
import rx.util.functions.Func1;


public final class Resources {

    /**
     * Access the response to writing the given resource as a resource in its
     * own right.
     */
    public static <RSRC, RESPONSE> ReadableResource<RESPONSE> asReadableResource(
            final WritableResource<RSRC, RESPONSE> resource,
            @Nullable final RSRC newValue) {
        Preconditions.checkNotNull(resource);

        final ReadableResource<RESPONSE> wrapper = new ReadableResource<RESPONSE>() {
            @Override
            public Observable<RESPONSE> get() {
                final Observable<RESPONSE> response = resource.write(newValue);
                return response;
            }
        };
        return wrapper;
    }

    /**
     * Access the response to deleting the given resource as a resource in its
     * own right.
     */
    public static <RESPONSE> ReadableResource<RESPONSE> asReadableResource(
            final DeletableResource<RESPONSE> resource) {
        Preconditions.checkNotNull(resource);

        final ReadableResource<RESPONSE> wrapper = new ReadableResource<RESPONSE>() {
            @Override
            public Observable<RESPONSE> get() {
                final Observable<RESPONSE> response = resource.delete();
                return response;
            }
        };
        return wrapper;
    }

    public static <FROM, TO> ReadableResource<TO> mapValue(
            final ReadableResource<? extends FROM> resource,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return mapValue(resource, rxFunc);
    }

    public static <FROM, TO> ReadableResource<TO> mapValue(
            final ReadableResource<? extends FROM> resource,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(resource, "null resource");
        Preconditions.checkNotNull(mapper, "null function");

        final ReadableResource<TO> result = new ReadableResource<TO>() {
            @Override
            public Observable<TO> get() {
                final Observable<? extends FROM> observable = resource.get();
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <FROM, TO> DeletableResource<TO> mapResponse(
            final DeletableResource<? extends FROM> resource,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return mapResponse(resource, rxFunc);
    }

    public static <FROM, TO> DeletableResource<TO> mapResponse(
            final DeletableResource<? extends FROM> resource,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(resource, "null resource");
        Preconditions.checkNotNull(mapper, "null function");

        final DeletableResource<TO> result = new DeletableResource<TO>() {
            @Override
            public Observable<TO> delete() {
                final Observable<? extends FROM> observable = resource.delete();
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <RSRC, FROM, TO> WritableResource<RSRC, TO> mapResponse(
            final WritableResource<? super RSRC, ? extends FROM> resource,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return mapResponse(resource, rxFunc);
    }

    public static <RSRC, FROM, TO> WritableResource<RSRC, TO> mapResponse(
            final WritableResource<? super RSRC, ? extends FROM> resource,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(resource, "null resource");
        Preconditions.checkNotNull(mapper, "null function");

        final WritableResource<RSRC, TO> result = new WritableResource<RSRC, TO>() {
            @Override
            public Observable<TO> write(final RSRC value) {
                final Observable<? extends FROM> observable = resource.write(value);
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <FROM, TO, RESPONSE> WritableResource<TO, RESPONSE> adaptNewValue(
            final WritableResource<? super FROM, RESPONSE> resource,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptNewValue(resource, rxFunc);
    }

    public static <FROM, TO, RESPONSE> WritableResource<TO, RESPONSE> adaptNewValue(
            final WritableResource<? super FROM, RESPONSE> resource,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(resource, "null resource");
        Preconditions.checkNotNull(adapter, "null function");

        final WritableResource<TO, RESPONSE> result = new WritableResource<TO, RESPONSE>() {
            @Override
            public Observable<RESPONSE> write(final TO value) {
                final FROM transformed = adapter.call(value);
                final Observable<RESPONSE> observable = resource.write(transformed);
                return observable;
            }
        };
        return result;
    }

    public static <UPDATE, FROM, TO> UpdatableResource<UPDATE, TO> map(
            final UpdatableResource<? super UPDATE, ? extends FROM> resource,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return map(resource, rxFunc);
    }

    public static <UPDATE, FROM, TO> UpdatableResource<UPDATE, TO> map(
            final UpdatableResource<? super UPDATE, ? extends FROM> resource,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(resource, "null resource");
        Preconditions.checkNotNull(mapper, "null function");

        final UpdatableResource<UPDATE, TO> result = new UpdatableResource<UPDATE, TO>() {
            @Override
            public Observable<TO> update(final UPDATE update) {
                final Observable<? extends FROM> observable = resource.update(update);
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <FROM, TO, RESPONSE> UpdatableResource<TO, RESPONSE> adaptUpdate(
            final UpdatableResource<? super FROM, RESPONSE> resource,
            final Function<? super TO, ? extends FROM> adapter) {
        final Func1<TO, FROM> rxFunc = new GuavaToRxFunction<TO, FROM>(adapter);
        return adaptUpdate(resource, rxFunc);
    }

    public static <FROM, TO, RESPONSE> UpdatableResource<TO, RESPONSE> adaptUpdate(
            final UpdatableResource<? super FROM, RESPONSE> resource,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(resource, "null resource");
        Preconditions.checkNotNull(adapter, "null function");

        final UpdatableResource<TO, RESPONSE> result = new UpdatableResource<TO, RESPONSE>() {
            @Override
            public Observable<RESPONSE> update(final TO update) {
                final FROM transformed = adapter.call(update);
                final Observable<RESPONSE> observable = resource.update(transformed);
                return observable;
            }
        };
        return result;
    }

    private Resources() {
        // prevent instantiation
    }

}

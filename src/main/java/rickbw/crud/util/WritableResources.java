package rickbw.crud.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.ReadableResource;
import rickbw.crud.WritableResource;
import rickbw.crud.util.rx.GuavaToRxFunction;
import rx.Observable;
import rx.util.functions.Func1;


/**
 * Utility methods pertaining to {@link WritableResource}s.
 */
public final class WritableResources {

    /**
     * Access the response to writing the given resource as a resource in its
     * own right.
     */
    public static <RSRC, RESPONSE> ReadableResource<RESPONSE> asReadableResource(
            final WritableResource<RSRC, RESPONSE> resource,
            final RSRC newValue) {
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

    // TODO: Expose other Observable methods

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

    private WritableResources() {
        // prevent instantiation
    }

}

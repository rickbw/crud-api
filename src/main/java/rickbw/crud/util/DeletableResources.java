package rickbw.crud.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.DeletableResource;
import rickbw.crud.ReadableResource;
import rickbw.crud.util.rx.GuavaToRxFunction;
import rx.Observable;
import rx.util.functions.Func1;


/**
 * Utility methods pertaining to {@link DeletableResource}s.
 */
public final class DeletableResources {

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

    // TODO: Expose other Observable methods

    private DeletableResources() {
        // prevent instantiation
    }

}

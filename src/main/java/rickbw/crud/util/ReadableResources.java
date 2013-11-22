package rickbw.crud.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.ReadableResource;
import rickbw.crud.util.rx.GuavaToRxFunction;
import rx.Observable;
import rx.util.functions.Func1;


/**
 * Utility methods pertaining to {@link ReadableResources}s.
 */
public final class ReadableResources {

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

    // TODO: Expose other Observable methods

    private ReadableResources() {
        // prevent instantiation
    }

}

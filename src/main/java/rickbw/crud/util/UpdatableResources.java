package rickbw.crud.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rickbw.crud.ReadableResource;
import rickbw.crud.UpdatableResource;
import rickbw.crud.util.rx.GuavaToRxFunction;
import rx.Observable;
import rx.util.functions.Func1;


/**
 * Utility methods pertaining to {@link UpdatableResource}s.
 *
 * You may notice that there is a method
 * {@link WritableResources#asReadableResource(rickbw.crud.WritableResource, Object)}
 * and a method
 * {@link DeletableResources#asReadableResource(rickbw.crud.DeletableResource)};
 * however, there is no <code>UpdatableResources.asReadableResource<code>.
 * This is by design: Read, Write, and Delete are all idempotent operations;
 * Update is not. To expose an Update as a Read would be to violate the
 * contract of {@link ReadableResource}.
 */
public final class UpdatableResources {

    public static <UPDATE, FROM, TO> UpdatableResource<UPDATE, TO> mapResponse(
            final UpdatableResource<? super UPDATE, ? extends FROM> resource,
            final Function<? super FROM, ? extends TO> mapper) {
        final Func1<FROM, TO> rxFunc = new GuavaToRxFunction<FROM, TO>(mapper);
        return mapResponse(resource, rxFunc);
    }

    public static <UPDATE, FROM, TO> UpdatableResource<UPDATE, TO> mapResponse(
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

    // TODO: Expose other Observable methods

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

    private UpdatableResources() {
        // prevent instantiation
    }

}

package rickbw.crud.util;

import com.google.common.base.Preconditions;

import rickbw.crud.DeletableResource;
import rx.Observable;
import rx.util.functions.Func1;


/**
 * A set of fluent transformations on {@link DeletableResource}s.
 */
public abstract class FluentDeletableResource<RESPONSE> implements DeletableResource<RESPONSE> {

    public static <RESPONSE> FluentDeletableResource<RESPONSE> from(final DeletableResource<RESPONSE> resource) {
        if (resource instanceof FluentDeletableResource<?>) {
            return (FluentDeletableResource<RESPONSE>) resource;
        } else {
            Preconditions.checkNotNull(resource);
            return new FluentDeletableResource<RESPONSE>() {
                @Override
                public Observable<RESPONSE> delete() {
                    return resource.delete();
                }
            };
        }
    }

    /**
     * Access the response to deleting the given resource as a resource in its
     * own right.
     */
    public FluentReadableResource<RESPONSE> asReadableResource() {
        final FluentReadableResource<RESPONSE> wrapper = new FluentReadableResource<RESPONSE>() {
            @Override
            public Observable<RESPONSE> get() {
                // FIXME: Memoize the result so we don't delete over and over!
                final Observable<RESPONSE> response = delete();
                return response;
            }
        };
        return wrapper;
    }

    public <TO> FluentDeletableResource<TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentDeletableResource<TO> result = new FluentDeletableResource<TO>() {
            @Override
            public Observable<TO> delete() {
                final Observable<? extends RESPONSE> observable = outerResource().delete();
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
        final DeletableResource<TO> newDelegate = result;
        return from(newDelegate);
    }

    // TODO: Expose other Observable methods

    // TODO: Adapt Subscriber

    private FluentDeletableResource<RESPONSE> outerResource() {
        return this;
    }

}

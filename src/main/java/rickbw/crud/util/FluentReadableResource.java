package rickbw.crud.util;

import rickbw.crud.ReadableResource;
import rx.Observable;
import rx.util.functions.Func1;


/**
 * A set of fluent transformations on {@link ReadableResource}s.
 */
public abstract class FluentReadableResource<RSRC> implements ReadableResource<RSRC> {

    public static <RSRC> FluentReadableResource<RSRC> from(final ReadableResource<RSRC> resource) {
        if (resource instanceof FluentReadableResource<?>) {
            return (FluentReadableResource<RSRC>) resource;
        } else {
            Preconditions.checkNotNull(resource);
            return new FluentReadableResource<RSRC>() {
                @Override
                public Observable<RSRC> get() {
                    return resource.get();
                }
            };
        }
    }

    public <TO> FluentReadableResource<TO> mapValue(final Func1<? super RSRC, ? extends TO> mapper) {
        Preconditions.checkNotNull(mapper, "null function");
        return new FluentReadableResource<TO>() {
            @Override
            public Observable<TO> get() {
                final Observable<? extends RSRC> observable = outerResource().get();
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
    }

    // TODO: Expose other Observable methods

    // TODO: Adapt Subscriber

    private FluentReadableResource<RSRC> outerResource() {
        return this;
    }

}

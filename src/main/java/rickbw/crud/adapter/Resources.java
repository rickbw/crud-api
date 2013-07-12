package rickbw.crud.adapter;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import rickbw.crud.DeletableResource;
import rickbw.crud.ReadableResource;
import rickbw.crud.WritableResource;
import rx.Observable;


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
    public static <RSRC, RESPONSE> ReadableResource<RESPONSE> asReadableResource(
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

    private Resources() {
        // prevent instantiation
    }

}

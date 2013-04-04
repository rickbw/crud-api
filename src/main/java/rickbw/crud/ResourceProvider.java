package rickbw.crud;

import java.util.concurrent.Future;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Provides asynchronous access to a resource.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 *
 * @param <RSRC>    The type of the resources.
 *
 * @see MapResourceProvider
 */
public interface ResourceProvider<RSRC> {

    /**
     * Issue an asynchronous request to get the value of the resource. That
     * resource will be delivered to the given {@link ResourceConsumer} when
     * it is ready.
     *
     * @return A future that will return null from {@link Future#get()} on
     *         success or throw on failure. This is useful for clients that
     *         need to block for the operation to complete before continuing.
     */
    public abstract ListenableFuture<?> get(
            ResourceConsumer<? super RSRC> consumer);

}

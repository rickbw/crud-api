package rickbw.crud;

import java.util.concurrent.Future;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Provides asynchronous access to a set of "resources", each identified
 * by a unique key. Keys are of uniform type, but the types of resources
 * depend on their keys.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 */
public interface ResourceProvider<KEY> {

    /**
     * Issue an asynchronous request to get the value of the resource
     * identified by the given key. That resource, and the original key, will
     * be delivered to the given ResourceProvider when it is ready. The
     * "returned" key makes it more convenient to reuse the same consumer
     * across multiple requests while still keeping track of which response
     * goes with which request.
     *
     * @param  <RSRC>   The expected type of the identified resource.
     *
     * @return A future that will return null from {@link Future#get()} on
     *         success or throw on failure. This is useful for clients that
     *         need to block for the operation to complete before continuing.
     *
     * @throws IllegalArgumentException         If the request is malformed --
     *         either the key itself is malformed or the combination of the
     *         key with some other state of this ResourceProvider is
     *         inappropriate.
     * @throws UnsupportedOperationException    If this provider does not
     *         support get operations.
     */
    public abstract <RSRC> ListenableFuture<?> get(
            KEY key,
            Class<? extends RSRC> resourceClass,
            ResourceConsumer<? super KEY, RSRC> consumer);

}

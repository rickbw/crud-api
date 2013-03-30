package rickbw.crud;

import java.util.Collection;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Provides generic asynchronous access to a set of values, each identified
 * by a unique key. Keys are of uniform type, but values may not be.
 *
 * As in the {@link Collection} interfaces, operations are optional for
 * implementers and may throw {@link UnsupportedOperationException}.
 *
 * XXX: Should operations take a second consumer for handling failures?
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

    /**
     * Set the value of the resource identified by the given key to the
     * given one, completely replacing the previous value (if any). The
     * operation will return some response upon completion of the request.
     * That response may have the same or different type as the type of the
     * resource itself.
     *
     * @param  <RSRC>   The expected type of the identified resource.
     * @param  <RESP>   The expected type of the response.
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
    public abstract <RSRC, RESP> ListenableFuture<?> set(
            KEY key,
            RSRC newValue,
            Class<? extends RESP> responseClass,
            ResourceConsumer<? super KEY, RESP> consumer);

    /**
     * Perform a partial update of the identified resource. The operation will
     * return some response upon completion of the request. That response may
     * have the same or different type as the type of the resource itself.
     *
     * @param  <UP>     The type of the partial update to be applied.
     * @param  <RESP>   The expected type of the response.
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
    public abstract <UP, RESP> ListenableFuture<?> update(
            KEY key,
            UP resourceUpdate,
            Class<? extends RESP> responseClass,
            ResourceConsumer<? super KEY, RESP> consumer);

    /**
     * Delete the resource identified by the given key. The operation will
     * return some response upon completion of the request. That response may
     * have the same or different type as the type of the resource itself.
     *
     * @param  <RESP>   The value type of the response.
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
    public abstract <RESP> ListenableFuture<?> delete(
            KEY key,
            Class<? extends RESP> responseClass,
            ResourceConsumer<? super KEY, RESP> consumer);

}

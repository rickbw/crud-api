package rickbw.crud;

import java.util.concurrent.Future;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Asynchronously updates a set of "resources", each identified by a unique
 * key. Keys are of uniform type, but the types of resources depend on their
 * keys.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 */
public interface ResourceUpdater<KEY> {

    /**
     * Update the value of the resource identified by the given key based on
     * the given update value, which may represent an entirely new state of
     * the resource or a partial update. The operation will return some
     * response upon completion of the request. That response may have the
     * same or different type as the type of the resource itself.
     *
     * @param  <UP>     The type of the update to apply to the resource.
     * @param  <RESP>   The expected type of the response.
     *
     * @return A future that will return null from {@link Future#get()} on
     *         success or throw on failure. This is useful for clients that
     *         need to block for the operation to complete before continuing.
     *
     * @throws IllegalArgumentException         If the request is malformed --
     *         either the key itself is malformed or the combination of the
     *         key with some other state of this ResourceUpdater is
     *         inappropriate.
     * @throws UnsupportedOperationException    If this provider does not
     *         support get operations.
     */
    public abstract <UP, RESP> ListenableFuture<?> apply(
            KEY key,
            UP update,
            Class<? extends RESP> responseClass,
            ResourceConsumer<? super KEY, RESP> consumer);

}

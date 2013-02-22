package rickbw.crud;

import java.util.Collection;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Provides generic asynchronous access to a set of values, each identified
 * by a unique key. Keys and values are each of uniform types.
 *
 * As in the {@link Collection} interfaces, operations are optional for
 * implementers and may throw {@link UnsupportedOperationException}.
 *
 * @param <K> The type of the keys. XXX: Should there be 2 parameters, one
 *        for success and one for failure?
 * @param <V> The type of the values. XXX: Should there be 2 parameters, one
 *        for success and one for failure?
 */
public interface ResourceProvider<K, V> {

    /**
     * Issue an asynchronous request to get the value of the resource
     * identified by the given key. That resource, and the original key, will
     * be delivered to the given ResourceProvider when it is ready. The
     * "returned" key makes it more convenient to reuse the same consumer
     * across multiple requests while still keeping track of which response
     * goes with which request.
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
    public abstract ListenableFuture<?> get(K key, ResourceConsumer<? super K, ? super V> consumer);

    /**
     * Set the value of the resource identified by the given key to the
     * given one, completely replacing the previous value (if any). The
     * operation will return some response upon completion of the request.
     * That response may have the same or different type as the type of the
     * resource itself.
     *
     * FIXME: KR and VR should really be parameters of ResourceProvider
     * itself; they should not vary by invocation of this method.
     *
     * @param  <VR> The value type of the response.
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
    public abstract <VR> ListenableFuture<?> set(K key, V newValue, ResourceConsumer<? super K, VR> consumer);

    /**
     * Perform a partial update of the identified resource. The operation will
     * return some response upon completion of the request. That response may
     * have the same or different type as the type of the resource itself.
     *
     * FIXME: VU, KR, and VR should really be parameters of ResourceProvider
     * itself; they should not vary by invocation of this method.
     *
     * @param  <VU> The type of the partial update to be applied.
     * @param  <VR> The value type of the response.
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
    public abstract <VU, VR> ListenableFuture<?> update(K key, VU valueUpdate, ResourceConsumer<? super K, VR> consumer);

    /**
     * Delete the resource identified by the given key. The operation will
     * return some response upon completion of the request. That response may
     * have the same or different type as the type of the resource itself.
     *
     * FIXME: KR and VR should really be parameters of ResourceProvider
     * itself; they should not vary by invocation of this method.
     *
     * @param  <VR> The value type of the response.
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
    public abstract <VR> ListenableFuture<?> delete(K key, ResourceConsumer<? super K, VR> consumer);

}

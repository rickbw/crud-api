package rickbw.crud;

import java.util.concurrent.Future;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Performs and asynchronous update of a resource. The update may be full or
 * partial, depending on the implementation.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 */
public interface ResourceUpdater<UPDATE, RESPONSE> {

    /**
     * Update the value of the resource in an unspecified way based on the
     * given value. The operation will return some response upon completion
     * of the request. That response may have the same or different type as
     * the type of the resource itself.
     *
     * @return A future that will return null from {@link Future#get()} on
     *         success or throw on failure. This is useful for clients that
     *         need to block for the operation to complete before continuing.
     *
     * @throws IllegalArgumentException         If the request is malformed --
     *         either the key itself is malformed or the combination of the
     *         key with some other state of this MapResourceSetter is
     *         inappropriate.
     */
    public abstract ListenableFuture<?> apply(
            UPDATE update,
            ResourceConsumer<? super RESPONSE> consumer);

}

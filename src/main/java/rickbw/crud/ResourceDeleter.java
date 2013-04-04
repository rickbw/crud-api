package rickbw.crud;

import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Deletes a resource asynchronously.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 *
 * @param <RESPONSE>    The type of the deletion response.
 *
 * @see MapResourceDeleter
 */
public interface ResourceDeleter<RESPONSE> {

    /**
     * Issue an asynchronous request to delete the value of the resource.
     * A response will be delivered to the given {@link ResourceConsumer}
     * when it is ready. The response may or may not be of the same type as
     * the resource itself, depending on the implementation.
     *
     * @return A future that will return null from {@link Future#get()} on
     *         success or throw on failure. This is useful for clients that
     *         need to block for the operation to complete before continuing.
     */
    public abstract ListenableFuture<?> delete(
            @Nullable ResourceConsumer<? super RESPONSE> consumer);

}

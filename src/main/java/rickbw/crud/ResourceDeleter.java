package rickbw.crud;

import javax.annotation.Nullable;


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
     */
    public abstract void delete(
            @Nullable ResourceConsumer<? super RESPONSE> consumer);

}

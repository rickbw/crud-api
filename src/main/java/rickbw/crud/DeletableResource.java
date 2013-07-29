package rickbw.crud;

import rx.Observable;


/**
 * Allows a client to delete the value from a resource. Any resource whose
 * value can be deleted should implement this interface.
 *
 * @param <RESPONSE>    The type of the deletion response(s), if any.
 *
 * @see ReadableResource
 * @see WritableResource
 * @see UpdatableResource
 * @see DeletableResourceProvider
 */
public interface DeletableResource<RESPONSE> extends Resource {

    /**
     * Issue a (possibly asynchronous) request to delete the value of this
     * resource. A response will be delivered when it is ready. The response
     * may or may not be of the same type as the resource itself, depending
     * on the implementation.
     *
     * This operation is idempotent.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RESPONSE> delete();

}

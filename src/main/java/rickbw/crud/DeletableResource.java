package rickbw.crud;

import rx.Observable;


/**
 * Allows a client to delete the value of a resource.
 *
 * @param <RESPONSE>    The type of the deletion response, if any.
 */
public interface DeletableResource<RESPONSE> {

    /**
     * Issue a (possibly asynchronous) request to delete the value of the
     * resource. A response will be delivered when it is ready. The response
     * may or may not be of the same type as the resource itself, depending
     * on the implementation.
     */
    public abstract Observable<RESPONSE> delete();

}

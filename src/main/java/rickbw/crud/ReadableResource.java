package rickbw.crud;

import rx.Observable;


/**
 * Allows a client to read the value(s) of a resource.
 *
 * @param <RSRC>    The type of the resource's value(s).
 */
public interface ReadableResource<RSRC> {

    /**
     * Issue an asynchronous request to get the value of the resource. That
     * resource will be delivered when it is ready.
     */
    public abstract Observable<RSRC> get();

}

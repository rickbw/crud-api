package rickbw.crud;

import rx.Observable;


/**
 * Allows a client to replace the value of a resource. If the resource
 * previously had no value, it will be initialized with the provided value.
 *
 * @param <RSRC>        The type of this resource's value.
 * @param <RESPONSE>    The type of the response(s) that may be returned as
 *        a result of the change in the value.
 */
public interface WritableResource<RSRC, RESPONSE> {

    public abstract Observable<RESPONSE> write(RSRC resource);

}

package rickbw.crud;

import rx.Observable;


/**
 * Allows a client to read the value(s) of a resource. Any resource whose
 * value can be read should implement this interface.
 *
 * @param <RSRC>    The type of the resource's value(s).
 *
 * @see WritableResource
 * @see UpdatableResource
 * @see DeletableResource
 * @see ReadableResourceProvider
 */
public interface ReadableResource<RSRC> extends Resource {

    /**
     * Request the value(s) of this resource. Zero or more values will be
     * delivered when they are ready.
     *
     * This operation is idempotent.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RSRC> get();

}

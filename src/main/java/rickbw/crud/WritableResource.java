package rickbw.crud;

import javax.annotation.Nullable;

import rx.Observable;


/**
 * Allows a client to replace the value of a resource. Any resource that
 * supports setting a new value should implement this interface.
 *
 * @param <RSRC>        The type of this resource's value.
 * @param <RESPONSE>    The type of the response(s) that may be returned as
 *        a result of the change in the value.
 *
 * @see ReadableResource
 * @see UpdatableResource
 * @see DeletableResource
 * @see WritableResourceProvider
 */
public interface WritableResource<RSRC, RESPONSE> extends Resource {

    /**
     * Replace the value of the resource with the given value. If the resource
     * previously had no value, it will be initialized with the provided
     * value. The operation may return one or more responses via the
     * given {@link Observable} as the request is acted upon.
     *
     * This operation is idempotent.
     *
     * @throws IllegalArgumentException If the new value is malformed in some
     *         way that is detectable at invocation time.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RESPONSE> write(@Nullable RSRC newValue);

}

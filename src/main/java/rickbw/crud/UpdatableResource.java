package rickbw.crud;

import rx.Observable;


/**
 * Allows a client to perform a partial update of the value of a resource.
 *
 * @param <UPDATE>  The type of the update to be applied to the value of the
 *        resource. It may or may not be the same as the type of the resource
 *        itself.
 * @param <RESPONSE>    The type of the response(s) that may come back as a
 *        result of the update. It may or may not be the same as the type of
 *        the resource itself.
 */
public interface UpdatableResource<UPDATE, RESPONSE> extends Resource {

    /**
     * Update the value of the resource in an unspecified way based on the
     * given value. The operation may return one or more responses via the
     * given {@link Observable} as the response is acted upon.
     *
     * @throws IllegalArgumentException If the request is malformed in some way.
     */
    public abstract Observable<RESPONSE> update(UPDATE update);

}

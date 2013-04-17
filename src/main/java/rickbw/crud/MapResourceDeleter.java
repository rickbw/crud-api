package rickbw.crud;


/**
 * Deletes a resources from a set of uniformly typed resources based on a
 * unique key. Keys are also of uniform type.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 *
 * @param <KEY>         The type of the keys.
 * @param <RESPONSE>    The type of the deletion response.
 *
 * @see ResourceDeleter
 */
public interface MapResourceDeleter<KEY, RESPONSE> {

    /**
     * Issue an asynchronous request to delete the value of the resource
     * identified by the given key. A response, with the original key, will
     * be delivered to the given {@link MapResourceConsumer} when it is ready.
     * The response may or may not be of the same type as the resource itself,
     * depending on the implementation. The "returned" key makes it more
     * convenient to reuse the same consumer across multiple requests while
     * still keeping track of which response goes with which request.
     *
     * @throws IllegalArgumentException         If the request is malformed --
     *         either the key itself is malformed or the combination of the
     *         key with some other state of this MapResourceProvider is
     *         inappropriate.
     */
    public abstract void delete(
            KEY key,
            MapResourceConsumer<? super KEY, ? super RESPONSE> consumer);

}

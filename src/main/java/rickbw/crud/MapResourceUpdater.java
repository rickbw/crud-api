package rickbw.crud;


/**
 * Perform an asynchronous update of the state of a resource identified by a
 * unique key. The update may be full or partial, depending on the
 * implementation. Resources and keys are of uniform type.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 */
public interface MapResourceUpdater<KEY, UPDATE, RESPONSE> {

    /**
     * Updates the value of the resource identified by the given key in an the
     * unspecified way, based on the given value. The operation will return
     * some response upon completion of the request. That response may have
     * the same or different type as the type of the resource itself.
     *
     * @throws IllegalArgumentException         If the request is malformed --
     *         either the key itself is malformed or the combination of the
     *         key with some other state of this MapResourceSetter is
     *         inappropriate.
     */
    public abstract void apply(
            KEY key,
            UPDATE update,
            MapResourceConsumer<? super KEY, ? super RESPONSE> consumer);

}

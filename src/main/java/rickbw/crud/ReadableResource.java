package rickbw.crud;


/**
 * Provides asynchronous access to a resource.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 *
 * @param <RSRC>    The type of the resources.
 */
public interface ReadableResource<RSRC> {

    /**
     * Issue an asynchronous request to get the value of the resource. That
     * resource will be delivered to the given {@link ResourceConsumer} when
     * it is ready.
     */
    public abstract void get(
            ResourceConsumer<? super RSRC> consumer);

}

package rickbw.crud;


/**
 * Performs a full update of a resource.
 *
 * XXX: Should the operation take a second consumer for handling failures?
 */
public interface WritableResource<RSRC, RESPONSE> {

    public abstract void write(
            RSRC resource,
            ResourceConsumer<? super RESPONSE> consumer);

}

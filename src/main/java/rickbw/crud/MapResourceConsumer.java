package rickbw.crud;


/**
 * A consumer of resources, where each resource is identified by a unique
 * key. Keys and resources of both of uniform type.
 *
 * @param <KEY>     The type of the keys.
 * @param <RSRC>    The type of the resources.
 *
 * @see ResourceConsumer
 */
public interface MapResourceConsumer<KEY, RSRC> {

    public abstract void accept(KEY key, RSRC value);

}

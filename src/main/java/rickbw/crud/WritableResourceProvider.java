package rickbw.crud;


/**
 * Look up a {@link WritableResource} based on a given key.
 *
 * @see ReadableResourceProvider
 * @see DeletableResourceProvider
 * @see UpdatableResourceProvider
 * @see WritableResource
 */
public interface WritableResourceProvider<KEY, RSRC, RESPONSE> extends ResourceProvider<KEY> {

    @Override
    public abstract WritableResource<RSRC, RESPONSE> get(KEY key);

}

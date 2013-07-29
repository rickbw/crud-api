package rickbw.crud;


/**
 * Look up a {@link ReadableResource} based on a given key.
 *
 * @see DeletableResourceProvider
 * @see WritableResourceProvider
 * @see UpdatableResourceProvider
 * @see ReadableResource
 */
public interface ReadableResourceProvider<KEY, RSRC> extends ResourceProvider<KEY> {

    @Override
    public abstract ReadableResource<RSRC> get(KEY key);

}

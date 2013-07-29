package rickbw.crud;


/**
 * Look up a {@link DeletableResource} based on a given key.
 *
 * @see ReadableResourceProvider
 * @see WritableResourceProvider
 * @see UpdatableResourceProvider
 * @see DeletableResource
 */
public interface DeletableResourceProvider<KEY, RESPONSE> extends ResourceProvider<KEY> {

    @Override
    public abstract DeletableResource<RESPONSE> get(KEY key);

}

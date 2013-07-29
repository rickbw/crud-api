package rickbw.crud;


/**
 * Look up a {@link UpdatableResource} based on a given key.
 *
 * @see ReadableResourceProvider
 * @see DeletableResourceProvider
 * @see WritableResourceProvider
 * @see UpdatableResource
 */
public interface UpdatableResourceProvider<KEY, UPDATE, RESPONSE> extends ResourceProvider<KEY> {

    @Override
    public abstract UpdatableResource<UPDATE, RESPONSE> get(KEY key);

}

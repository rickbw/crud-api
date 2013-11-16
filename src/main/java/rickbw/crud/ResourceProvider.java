package rickbw.crud;


/**
 * Look up a resource based on a given key. This super-interface encapsulates
 * navigation among resources. It is designed so that implementations that
 * return resources that implement multiple interfaces (e.g. that are both a
 * {@link ReadableResource} and a {@link WritableResource}) can extend
 * multiple sub-interfaces of this interface (e.g. both
 * {@link ReadableResourceProvider} and {@link WritableResourceProvider}) by
 * overriding the return type of {@link #get(Object)}.
 */
public interface ResourceProvider<KEY> {

    /**
     * Get the {@link Resource} associated with the given key.
     *
     * This operation is assumed to be relatively cheap and in-memory.
     * Specifically, when dealing with remote and/or persistent Resources,
     * implementers should locate expensive operations -- such as transferring
     * data across a network -- within the Resource implementations
     * themselves, and leave the ResourceProviders to simply prepare the
     * necessary objects.
     *
     * @throws NullPointerException if the given key is null.
     */
    public abstract Resource get(KEY key);

    /**
     * Two ResourceProviders are considered equal if the would return the
     * same {@link Resource}s for the same keys.
     */
    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

}

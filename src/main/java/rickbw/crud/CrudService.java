package rickbw.crud;

import java.util.Collection;


/**
 * Provides generic asynchronous access to a set of resources, each identified
 * by a unique key. Keys are of uniform type, but the types of resources
 * depend on their keys.
 *
 * As in the {@link Collection} interfaces, operations are optional for
 * implementers and may throw {@link UnsupportedOperationException}.
 */
public interface CrudService<KEY> {

    /**
     * Allow resource retrieval.
     *
     * @throws UnsupportedOperationException    If this service does not
     *         support "Get"/"Read" operations.
     */
    public ResourceProvider<KEY> provider();

    /**
     * Allows replacing the values of resources with entirely new ones,
     * completely replacing any previous value and creating resources if they
     * did not previously exist.
     *
     * @throws UnsupportedOperationException    If this service does not
     *         support "Set"/"Create" operations.
     */
    public ResourceUpdater<KEY> setter();

    /**
     * Allows the partial updating of resources.
     *
     * @throws UnsupportedOperationException    If this service does not
     *         support "Update" operations.
     */
    public ResourceUpdater<KEY> updater();

    /**
     * Allows the deletion of resources. Although the {@link ResourceUpdater}
     * interface allows passing a "new value" to
     * {@link ResourceUpdater#apply(Object, Object, Class, ResourceConsumer)},
     * implementors are highly recommended to not use these values, and
     * callers are highly recommended to pass null.
     *
     * @throws UnsupportedOperationException    If this service does not
     *         support "Delete" operations.
     */
    public ResourceUpdater<KEY> deleter();

}

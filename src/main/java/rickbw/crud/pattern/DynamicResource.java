package rickbw.crud.pattern;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rickbw.crud.UpdatableResource;
import rickbw.crud.UpdatableResourceProvider;
import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;


/**
 * A resource that supports all CRUD operations -- or whose support for said
 * operations cannot be determined until run time. This situation is similar
 * to that of an HTTP resource, to which any of the HTTP action requests may
 * be sent, but which may respond with a 400-class response in the event that
 * a given action is not available.
 */
public interface DynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP>
extends ReadableResource<RSRC>,
        WritableResource<RSRC, WR_RESP>,
        UpdatableResource<UPDATE, UP_RESP>,
        DeletableResource<DEL_RESP> {

    public static interface Provider<KEY, RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP>
    extends ReadableResourceProvider<KEY, RSRC>,
            WritableResourceProvider<KEY, RSRC, WR_RESP>,
            UpdatableResourceProvider<KEY, UPDATE, UP_RESP>,
            DeletableResourceProvider<KEY, DEL_RESP> {
        @Override
        public abstract DynamicResource<RSRC, UPDATE, WR_RESP, UP_RESP, DEL_RESP> get(KEY key);
    }

}

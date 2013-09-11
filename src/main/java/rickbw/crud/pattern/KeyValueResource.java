package rickbw.crud.pattern;

import java.util.Map;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rickbw.crud.Resource;
import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;


/**
 * A combination of {@link Resource} operations likely to occur when using a
 * key-value data store, such as a {@link Map} or a Voldemort store.
 */
public interface KeyValueResource<RSRC, WR_RESP, DEL_RESP>
extends ReadableResource<RSRC>,
        WritableResource<RSRC, WR_RESP>,
        DeletableResource<DEL_RESP> {

    public static interface Provider<KEY, RSRC, WR_RESP, DEL_RESP>
    extends ReadableResourceProvider<KEY, RSRC>,
            WritableResourceProvider<KEY, RSRC, WR_RESP>,
            DeletableResourceProvider<KEY, DEL_RESP> {
        @Override
        public abstract KeyValueResource<RSRC, WR_RESP, DEL_RESP> get(KEY key);
    }

}

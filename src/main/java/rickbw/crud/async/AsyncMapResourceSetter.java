package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncMapResourceSetter<KEY, RSRC, RESPONSE> {

    public abstract ListenableFuture<RESPONSE> putAsync(KEY key, RSRC update);

}

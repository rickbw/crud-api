package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncMapResourceProvider<KEY, RSRC> {

    public abstract ListenableFuture<RSRC> getAsync(KEY key);

}

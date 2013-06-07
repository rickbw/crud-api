package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public interface AsyncMapResourceProvider<KEY, RSRC> extends ResourceCloser<RSRC> {

    public abstract ListenableFuture<RSRC> getAsync(KEY key);

}

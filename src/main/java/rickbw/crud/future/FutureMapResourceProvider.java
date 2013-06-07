package rickbw.crud.future;

import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public interface FutureMapResourceProvider<KEY, RSRC> extends ResourceCloser<RSRC> {

    public abstract ListenableFuture<RSRC> getFuture(KEY key);

}

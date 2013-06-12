package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public interface AsyncMapResourceUpdater<KEY, UPDATE, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract ListenableFuture<RESPONSE> updateAsync(KEY key, UPDATE update);

}

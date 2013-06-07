package rickbw.crud.future;

import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public interface FutureMapResourceDeleter<KEY, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract ListenableFuture<RESPONSE> deleteFuture(KEY key);

}

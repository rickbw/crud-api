package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public interface AsyncResourceDeleter<RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract ListenableFuture<RESPONSE> deleteAsync();

}

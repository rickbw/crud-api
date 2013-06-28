package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncMapResourceUpdater<KEY, UPDATE, RESPONSE> {

    public abstract ListenableFuture<RESPONSE> updateAsync(KEY key, UPDATE update);

}

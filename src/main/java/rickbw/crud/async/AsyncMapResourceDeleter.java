package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncMapResourceDeleter<KEY, RESPONSE> {

    public abstract ListenableFuture<RESPONSE> deleteAsync(KEY key);

}

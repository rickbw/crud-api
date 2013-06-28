package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncResourceDeleter<RESPONSE> {

    public abstract ListenableFuture<RESPONSE> deleteAsync();

}

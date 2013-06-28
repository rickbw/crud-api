package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncDeletableResource<RESPONSE> {

    public abstract ListenableFuture<RESPONSE> deleteAsync();

}

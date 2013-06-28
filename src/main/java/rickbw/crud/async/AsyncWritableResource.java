package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncWritableResource<RSRC, RESPONSE> {

    public abstract ListenableFuture<RESPONSE> writeAsync(RSRC update);

}

package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncReadableResource<RSRC> {

    public abstract ListenableFuture<RSRC> getAsync();

}

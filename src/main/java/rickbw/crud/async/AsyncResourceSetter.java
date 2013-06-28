package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncResourceSetter<RSRC, RESPONSE> {

    public abstract ListenableFuture<RESPONSE> putAsync(RSRC update);

}

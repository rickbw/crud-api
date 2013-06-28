package rickbw.crud.async;

import com.google.common.util.concurrent.ListenableFuture;


public interface AsyncResourceUpdater<UPDATE, RESPONSE> {

    public abstract ListenableFuture<RESPONSE> updateAsync(UPDATE update);

}

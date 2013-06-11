package rickbw.crud.async;

import java.io.IOException;

import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public interface AsyncResourceUpdater<UPDATE, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract ListenableFuture<RESPONSE> updateAsync(UPDATE update) throws IOException;

}

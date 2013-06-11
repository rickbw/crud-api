package rickbw.crud.async;

import java.io.IOException;

import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public interface AsyncResourceSetter<UPDATE, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract ListenableFuture<RESPONSE> putAsync(UPDATE update) throws IOException;

}

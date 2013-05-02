package rickbw.crud.future;

import java.io.IOException;

import com.google.common.util.concurrent.ListenableFuture;


public interface FutureMapResourceProvider<KEY, RSRC> {

    public abstract ListenableFuture<RSRC> getFuture(KEY key);

    public abstract void close(RSRC resource) throws IOException;

}

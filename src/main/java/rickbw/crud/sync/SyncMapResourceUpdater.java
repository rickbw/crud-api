package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceUpdater<KEY, UPDATE, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract RESPONSE updateSync(KEY key, UPDATE update) throws IOException;

}

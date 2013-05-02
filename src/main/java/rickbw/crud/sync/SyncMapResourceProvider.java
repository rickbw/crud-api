package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceProvider<KEY, RSRC> {

    public abstract RSRC get(KEY key) throws IOException;

    public abstract void close(RSRC resource) throws IOException;

}

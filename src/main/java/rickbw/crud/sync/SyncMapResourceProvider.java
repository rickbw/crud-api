package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceProvider<KEY, RSRC> {

    public abstract RSRC getSync(KEY key) throws IOException;

}

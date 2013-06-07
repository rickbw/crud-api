package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceProvider<KEY, RSRC> extends ResourceCloser<RSRC> {

    public abstract RSRC getSync(KEY key) throws IOException;

}

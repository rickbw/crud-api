package rickbw.crud.sync;

import java.io.IOException;


public interface SyncResourceProvider<RSRC> {

    public abstract RSRC getSync() throws IOException;

}

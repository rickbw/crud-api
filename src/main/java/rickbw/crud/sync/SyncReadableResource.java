package rickbw.crud.sync;

import java.io.IOException;


public interface SyncReadableResource<RSRC> {

    public abstract RSRC getSync() throws IOException;

}

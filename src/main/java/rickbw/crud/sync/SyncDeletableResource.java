package rickbw.crud.sync;

import java.io.IOException;


public interface SyncDeletableResource<RESPONSE> {

    public abstract RESPONSE deleteSync() throws IOException;

}
